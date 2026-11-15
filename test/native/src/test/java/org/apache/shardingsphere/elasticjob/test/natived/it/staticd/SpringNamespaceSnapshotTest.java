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

import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.elasticjob.kernel.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Verifies that the Spring Namespace {@code elasticjob:snapshot} tag starts a snapshot service that dumps job data.
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceSnapshotTest.TestApp.class)
@EnabledInNativeImage
class SpringNamespaceSnapshotTest {
    
    private static int dumpPortValue;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertDumpJob() throws IOException {
        CoordinatorRegistryCenter regCenter = applicationContext.getBean(CoordinatorRegistryCenter.class);
        regCenter.persist("/snapshotNativeJob/config", "native");
        String actual = SnapshotService.dumpJob("127.0.0.1", dumpPort(), "snapshotNativeJob");
        assertThat(actual, containsString("/snapshotNativeJob/config | native"));
    }
    
    /**
     * Get the dump port. The port is chosen on first use, so that it is never baked into the AOT generated context.
     *
     * @return dump port
     */
    static synchronized int dumpPort() {
        if (0 == dumpPortValue) {
            dumpPortValue = InstanceSpec.getRandomPort();
        }
        return dumpPortValue;
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/snapshot.xml")
    static class TestApp {
        
        /**
         * The AOT build phase instantiates this post-processor as well. The dump port is only resolved
         * when the tests actually execute (native image), so that no stale port is baked into the generated context.
         *
         * @return property sources placeholder configurer
         */
        @Bean
        static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
            PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
            result.setIgnoreUnresolvablePlaceholders(true);
            if (null != System.getProperty("org.graalvm.nativeimage.imagecode")) {
                Properties props = new Properties();
                props.setProperty("snapshot.dump.port", String.valueOf(dumpPort()));
                result.setProperties(props);
            }
            return result;
        }
    }
}
