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

import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.namespace.NamespaceNativeSimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Verifies that jobs defined by the Spring Namespace run under GraalVM Native Image,
 * and that the one-off job writes its RDB tracing events.
 *
 * <p>The Spring Namespace defines its own tracing configuration, so the RDB tracing of the starter is disabled here.
 * Otherwise the starter would find two {@code TracingConfiguration} beans while creating the jobs of {@code application.yml}.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceJobTest.TestApp.class, properties = "elasticjob.tracing.type=NONE")
@EnabledInNativeImage
class SpringNamespaceJobTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertOneOffJobWithRdbTracing() {
        OneOffJobBootstrap jobBootstrap = applicationContext.getBean("namespaceNativeOneOffJob", OneOffJobBootstrap.class);
        NamespaceNativeSimpleJob job = applicationContext.getBean("namespaceNativeOneOffJobImpl", NamespaceNativeSimpleJob.class);
        assertThat(job.getExecutionCount(), is(0));
        jobBootstrap.execute();
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> job.getExecutionCount() == 1);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(applicationContext.getBean("elasticJobTracingDataSource", DataSource.class));
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JOB_EXECUTION_LOG", Integer.class) > 0);
    }
    
    @Test
    void assertScheduledJob() {
        NamespaceNativeSimpleJob job = applicationContext.getBean("namespaceNativeScheduledJobImpl", NamespaceNativeSimpleJob.class);
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> job.getExecutionCount() > 0);
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/job.xml")
    static class TestApp {
    }
}
