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

import org.apache.shardingsphere.elasticjob.test.natived.commons.http.LocalHttpServer;
import org.apache.shardingsphere.elasticjob.test.natived.commons.tracing.TracingAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Verifies that a job defined by the Spring Namespace with {@code job-type} as {@code HTTP} sends its requests
 * under GraalVM Native Image, and that the job writes its RDB tracing events.
 *
 * <p>The context has no auto-configuration and no web environment, as in {@link AnnotationSpringBootTest},
 * because only the namespace job and its tracing are under test.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceHttpJobTest.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledInNativeImage
class SpringNamespaceHttpJobTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @BeforeAll
    static void beforeAll() throws IOException {
        LocalHttpServer.start();
    }
    
    @AfterAll
    static void afterAll() {
        LocalHttpServer.stop();
    }
    
    @Test
    void assertHttpJobWithRdbTracing() {
        LocalHttpServer.awaitRequest("namespaceNativeHttpJob");
        TracingAssertions.awaitSuccessfulExecution(applicationContext.getBean("elasticJobTracingDataSource", DataSource.class), "namespaceNativeHttpJob");
    }
    
    @Configuration(proxyBeanMethods = false)
    @ImportResource("classpath:META-INF/native-namespace/http-job.xml")
    static class TestApp {
        
        /**
         * The AOT build phase instantiates this post-processor as well. The URI of the job is only resolved
         * when the tests actually execute (native image), so that no stale address is baked into the generated context.
         *
         * @return property sources placeholder configurer
         */
        @Bean
        static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
            PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
            result.setIgnoreUnresolvablePlaceholders(true);
            if (null != System.getProperty("org.graalvm.nativeimage.imagecode")) {
                Properties props = new Properties();
                props.setProperty("http.job.uri", LocalHttpServer.jobUri("namespaceNativeHttpJob"));
                result.setProperties(props);
            }
            return result;
        }
    }
}
