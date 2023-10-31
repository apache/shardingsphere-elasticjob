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

package org.apache.shardingsphere.elasticjob.spring.boot.job;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobScheduler;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.boot.job.fixture.job.impl.CustomTestJob;
import org.apache.shardingsphere.elasticjob.spring.boot.reg.ZookeeperProperties;
import org.apache.shardingsphere.elasticjob.spring.boot.tracing.TracingProperties;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.kernel.tracing.api.TracingConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.main.banner-mode=off")
@SpringBootApplication
@ActiveProfiles("elasticjob")
class ElasticJobSpringBootTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(18181);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
    }
    
    @Test
    void assertZookeeperProperties() {
        assertNotNull(applicationContext);
        ZookeeperProperties actual = applicationContext.getBean(ZookeeperProperties.class);
        assertThat(actual.getServerLists(), is(EMBED_TESTING_SERVER.getConnectionString()));
        assertThat(actual.getNamespace(), is("elasticjob-spring-boot-starter"));
    }
    
    @Test
    void assertRegistryCenterCreation() {
        assertNotNull(applicationContext);
        ZookeeperRegistryCenter zookeeperRegistryCenter = applicationContext.getBean(ZookeeperRegistryCenter.class);
        assertNotNull(zookeeperRegistryCenter);
        zookeeperRegistryCenter.persist("/foo", "bar");
        assertThat(zookeeperRegistryCenter.get("/foo"), is("bar"));
    }
    
    @Test
    void assertTracingConfigurationCreation() throws SQLException {
        assertNotNull(applicationContext);
        TracingConfiguration<?> tracingConfig = applicationContext.getBean(TracingConfiguration.class);
        assertNotNull(tracingConfig);
        assertThat(tracingConfig.getType(), is("RDB"));
        assertTrue(tracingConfig.getTracingStorageConfiguration().getStorage() instanceof DataSource);
        DataSource dataSource = (DataSource) tracingConfig.getTracingStorageConfiguration().getStorage();
        assertNotNull(dataSource.getConnection());
    }
    
    @Test
    void assertTracingProperties() {
        assertNotNull(applicationContext);
        TracingProperties tracingProperties = applicationContext.getBean(TracingProperties.class);
        assertNotNull(tracingProperties);
        assertTrue(tracingProperties.getIncludeJobNames().isEmpty());
        Set<String> excludeJobNames = new HashSet<>();
        excludeJobNames.add("customTestJob");
        assertThat(tracingProperties.getExcludeJobNames(), is(excludeJobNames));
    }
    
    @Test
    void assertElasticJobProperties() {
        assertNotNull(applicationContext);
        ElasticJobProperties elasticJobProperties = applicationContext.getBean(ElasticJobProperties.class);
        assertNotNull(elasticJobProperties);
        assertNotNull(elasticJobProperties.getJobs());
        assertThat(elasticJobProperties.getJobs().size(), is(4));
        ElasticJobConfigurationProperties customTestJobProperties = elasticJobProperties.getJobs().get("customTestJob");
        assertNotNull(customTestJobProperties);
        assertThat(customTestJobProperties.getElasticJobClass(), is(CustomTestJob.class));
        assertThat(customTestJobProperties.getJobBootstrapBeanName(), is("customTestJobBean"));
        assertThat(customTestJobProperties.getShardingTotalCount(), is(3));
        assertNull(customTestJobProperties.getElasticJobType());
        assertThat(customTestJobProperties.getJobListenerTypes().size(), is(2));
        assertThat(customTestJobProperties.getJobListenerTypes(), is(Arrays.asList("NOOP", "LOG")));
        ElasticJobConfigurationProperties printTestJobProperties = elasticJobProperties.getJobs().get("printTestJob");
        assertNotNull(printTestJobProperties);
        assertNull(printTestJobProperties.getElasticJobClass());
        assertThat(printTestJobProperties.getElasticJobType(), is("PRINT"));
        assertThat(printTestJobProperties.getJobBootstrapBeanName(), is("printTestJobBean"));
        assertThat(printTestJobProperties.getShardingTotalCount(), is(3));
        assertTrue(printTestJobProperties.getJobListenerTypes().isEmpty());
        assertThat(printTestJobProperties.getProps().size(), is(1));
        assertThat(printTestJobProperties.getProps().getProperty("print.content"), is("test print job"));
    }
    
    @Test
    void assertJobScheduleCreation() {
        Awaitility.await().atLeast(100L, TimeUnit.MILLISECONDS).atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> {
            assertNotNull(applicationContext);
            Map<String, ElasticJob> elasticJobBeans = applicationContext.getBeansOfType(ElasticJob.class);
            assertFalse(elasticJobBeans.isEmpty());
            Map<String, JobBootstrap> jobBootstrapBeans = applicationContext.getBeansOfType(JobBootstrap.class);
            assertFalse(jobBootstrapBeans.isEmpty());
        });
    }
    
    @Test
    void assertOneOffJobBootstrapBeanName() {
        assertNotNull(applicationContext);
        OneOffJobBootstrap customTestJobBootstrap = applicationContext.getBean("customTestJobBean", OneOffJobBootstrap.class);
        assertNotNull(customTestJobBootstrap);
        Collection<JobExtraConfiguration> extraConfigs = ((JobScheduler) ReflectionUtils.getFieldValue(customTestJobBootstrap, "jobScheduler")).getJobConfig().getExtraConfigurations();
        assertThat(extraConfigs.size(), is(0));
        OneOffJobBootstrap printTestJobBootstrap = applicationContext.getBean("printTestJobBean", OneOffJobBootstrap.class);
        extraConfigs = ((JobScheduler) ReflectionUtils.getFieldValue(printTestJobBootstrap, "jobScheduler")).getJobConfig().getExtraConfigurations();
        assertThat(extraConfigs.size(), is(1));
    }
    
    @Test
    void assertDefaultBeanNameWithClassJob() {
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean("defaultBeanNameClassJobScheduleJobBootstrap", ScheduleJobBootstrap.class));
    }
    
    @Test
    void assertDefaultBeanNameWithTypeJob() {
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean("defaultBeanNameTypeJobScheduleJobBootstrap", ScheduleJobBootstrap.class));
    }
}
