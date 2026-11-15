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
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.namespace.scanner.NamespaceScannerOneOffJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.namespace.scanner.NamespaceScannerScheduleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Verifies that jobs registered by the Spring Namespace {@code elasticjob:job-scanner} tag run under GraalVM Native Image.
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceJobScannerTest.TestApp.class)
@EnabledInNativeImage
class SpringNamespaceJobScannerTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertScheduleJobScanned() {
        NamespaceScannerScheduleJob job = applicationContext.getBean(NamespaceScannerScheduleJob.class);
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> job.getExecutionCount() > 0);
    }
    
    @Test
    void assertOneOffJobScanned() {
        OneOffJobBootstrap jobBootstrap = applicationContext.getBean("scannerOneOffJob", OneOffJobBootstrap.class);
        NamespaceScannerOneOffJob job = applicationContext.getBean(NamespaceScannerOneOffJob.class);
        assertThat(job.getExecutionCount(), is(0));
        jobBootstrap.execute();
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> job.getExecutionCount() == 1);
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/job-scanner.xml")
    static class TestApp {
    }
}
