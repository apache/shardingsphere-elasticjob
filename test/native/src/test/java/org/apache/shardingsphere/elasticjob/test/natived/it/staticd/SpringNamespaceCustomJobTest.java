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

import org.apache.shardingsphere.elasticjob.test.natived.commons.job.executor.NativeCustomJobExecutor;
import org.apache.shardingsphere.elasticjob.test.natived.commons.tracing.TracingAssertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * Verifies that a job defined by the Spring Namespace with a custom {@code job-type}, whose typed job item executor
 * is registered through SPI, runs under GraalVM Native Image, and that the job writes its RDB tracing events.
 *
 * <p>The context has no auto-configuration and no web environment, as in {@link AnnotationSpringBootTest},
 * because only the namespace job and its tracing are under test.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceCustomJobTest.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledInNativeImage
class SpringNamespaceCustomJobTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertCustomJobWithRdbTracing() {
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> NativeCustomJobExecutor.isExecuted("namespaceNativeCustomJob"));
        TracingAssertions.awaitSuccessfulExecution(applicationContext.getBean("elasticJobTracingDataSource", DataSource.class), "namespaceNativeCustomJob");
    }
    
    @Configuration(proxyBeanMethods = false)
    @ImportResource("classpath:META-INF/native-namespace/custom-job.xml")
    static class TestApp {
    }
}
