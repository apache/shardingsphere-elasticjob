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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.job;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.spring.boot.job.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.spring.boot.reg.ZookeeperProperties;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@SpringBootApplication
@ActiveProfiles("elasticjob")
public class ElasticJobSpringBootTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }

    @Test
    public void testZookeeperProperties() {
        assertNotNull(applicationContext);
        ZookeeperProperties zookeeperProperties = applicationContext.getBean(ZookeeperProperties.class);
        assertEquals(EmbedTestingServer.getConnectionString(), zookeeperProperties.getServerLists());
        assertEquals("elasticjob-lite-spring-boot-starter", zookeeperProperties.getNamespace());
    }

    @Test
    public void testRegistryCenterCreation() {
        assertNotNull(applicationContext);
        ZookeeperRegistryCenter zookeeperRegistryCenter = applicationContext.getBean(ZookeeperRegistryCenter.class);
        assertNotNull(zookeeperRegistryCenter);
        zookeeperRegistryCenter.persist("/foo", "bar");
        assertEquals("bar", zookeeperRegistryCenter.get("/foo"));
    }

    @Test
    public void testTracingConfigurationCreation() throws SQLException {
        assertNotNull(applicationContext);
        TracingConfiguration tracingConfiguration = applicationContext.getBean(TracingConfiguration.class);
        assertNotNull(tracingConfiguration);
        assertEquals("RDB", tracingConfiguration.getType());
        assertTrue(tracingConfiguration.getStorage() instanceof DataSource);
        DataSource dataSource = (DataSource) tracingConfiguration.getStorage();
        assertNotNull(dataSource.getConnection());
    }

    @Test
    public void testJobScheduleCreation() {
        assertNotNull(applicationContext);
        Map<String, ElasticJob> elasticJobBeans = applicationContext.getBeansOfType(ElasticJob.class);
        assertFalse(elasticJobBeans.isEmpty());
        Map<String, JobBootstrap> jobBootstrapBeans = applicationContext.getBeansOfType(JobBootstrap.class);
        assertFalse(jobBootstrapBeans.isEmpty());
        BlockUtils.waitingShortTime();
    }
}
