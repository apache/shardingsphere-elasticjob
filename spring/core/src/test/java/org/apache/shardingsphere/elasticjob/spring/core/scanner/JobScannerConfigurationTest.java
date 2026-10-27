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

package org.apache.shardingsphere.elasticjob.spring.core.scanner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobScannerConfigurationTest {
    
    @Test
    void assertAfterPropertiesSetWithBasePackages() {
        JobScannerConfiguration config = new JobScannerConfiguration(new String[]{"com.example"});
        assertDoesNotThrow(config::afterPropertiesSet);
    }
    
    @Test
    void assertAfterPropertiesSetFailedWithNull() {
        JobScannerConfiguration config = new JobScannerConfiguration(null);
        assertThrows(IllegalArgumentException.class, config::afterPropertiesSet);
    }
    
    @Test
    void assertPostProcessBeanDefinitionRegistry() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        JobScannerConfiguration config = new JobScannerConfiguration(
                new String[]{"org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture"});
        assertDoesNotThrow(() -> config.postProcessBeanDefinitionRegistry(registry));
        assertTrue(registry.containsBeanDefinition("scheduledScannerJob"));
        assertTrue(registry.containsBeanDefinition("oneOffScannerJob"));
    }
}
