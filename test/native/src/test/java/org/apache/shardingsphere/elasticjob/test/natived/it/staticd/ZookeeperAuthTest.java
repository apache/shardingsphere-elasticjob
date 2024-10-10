/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.test.natived.it.staticd;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
@Testcontainers
public class ZookeeperAuthTest {
    
    @SuppressWarnings("resource")
    @Container
    private static final GenericContainer<?> CONTAINER = new GenericContainer<>("zookeeper:3.9.2")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("test-native/conf/jaas-server-test-native.conf", Transferable.DEFAULT_FILE_MODE),
                    "/jaas-server-test-native.conf")
            .withEnv("JVMFLAGS", "-Djava.security.auth.login.config=/jaas-server-test-native.conf")
            .withEnv("ZOO_CFG_EXTRA", "authProvider.1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider sessionRequireClientSASLAuth=true")
            .withExposedPorts(2181);
    
    @BeforeAll
    static void beforeAll() {
        Configuration.setConfiguration(new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
                Map<String, String> conf = new HashMap<>();
                conf.put("username", "bob");
                conf.put("password", "bobsecret");
                AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
                entries[0] = new AppConfigurationEntry(
                        "org.apache.zookeeper.server.auth.DigestLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        conf);
                return entries;
            }
        });
    }
    
    @AfterAll
    static void afterAll() {
        Configuration.setConfiguration(null);
    }
    
    /**
     * For {@link org.apache.curator.test.TestingServer}, a lot of system properties are set in the background,
     * refer to
     * <a href="https://github.com/apache/zookeeper/blob/release-3.9.2/zookeeper-server/src/test/java/org/apache/zookeeper/test/SaslDigestAuthOverSSLTest.java">SaslDigestAuthOverSSLTest.java</a> .
     * Therefore, in order to test Zookeeper Server with SASL mechanism enabled under ElasticJob {@link CoordinatorRegistryCenter},
     * ElasticJob should never start Zookeeper Server through {@link org.apache.curator.test.TestingServer}.
     * Running Zookeeper Server and Curator Client in the same JVM process will pollute system properties.
     * For more information on this unit test,
     * refer to <a href="https://zookeeper.apache.org/doc/r3.9.2/zookeeperAdmin.html">ZooKeeper Administrator's Guide</a> and
     * <a href="https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeper+and+SASL">ZooKeeper and SASL</a> .
     *
     * @throws Exception exception
     */
    @Test
    void testSaslDigestMd5() throws Exception {
        String connectionString = CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(2181);
        Thread.sleep(Duration.ofSeconds(5L).toMillis());
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(
                new ZookeeperConfiguration(connectionString, "elasticjob-test-native-sasl-digest-md5"));
        regCenter.init();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");
        TracingConfiguration<DataSource> tracingConfig = new TracingConfiguration<>("RDB", new HikariDataSource(hikariConfig));
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(
                regCenter,
                new JavaSimpleJob(),
                JobConfiguration.newBuilder("testSaslDigestMd5", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
        regCenter.close();
    }
}
