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

import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture.ScannerScheduleJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassPathJobScannerTest {
    
    @Test
    void assertScanScheduleJob() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        scanner.scan("org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture");
        BeanDefinition scheduleJobBootstrap = registry.getBeanDefinition("scheduledScannerJob");
        assertThat(scheduleJobBootstrap.getBeanClassName(), is(ScheduleJobBootstrap.class.getName()));
        assertThat(scheduleJobBootstrap.getInitMethodName(), is("schedule"));
        ConstructorArgumentValues args = scheduleJobBootstrap.getConstructorArgumentValues();
        assertThat(args.getArgumentCount(), is(2));
    }
    
    @Test
    void assertScanOneOffJob() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        scanner.scan("org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture");
        BeanDefinition oneOffJobBootstrap = registry.getBeanDefinition("oneOffScannerJob");
        assertThat(oneOffJobBootstrap.getBeanClassName(), is(OneOffJobBootstrap.class.getName()));
        assertNull(oneOffJobBootstrap.getInitMethodName());
        ConstructorArgumentValues args = oneOffJobBootstrap.getConstructorArgumentValues();
        assertThat(args.getArgumentCount(), is(2));
    }
    
    @Test
    void assertScanJobWithCustomRegistryCenterAndLabel() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        scanner.scan("org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture");
        BeanDefinition customJobBootstrap = registry.getBeanDefinition("customRegistryScannerJob");
        assertNotNull(customJobBootstrap);
        assertThat(customJobBootstrap.getBeanClassName(), is(ScheduleJobBootstrap.class.getName()));
        assertThat(customJobBootstrap.getInitMethodName(), is("schedule"));
        assertThat(customJobBootstrap.getConstructorArgumentValues().getArgumentCount(), is(2));
    }
    
    @Test
    void assertScanEmptyPackage() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        scanner.scan("org.apache.shardingsphere.elasticjob.spring.core.util");
        assertThat(registry.containsBeanDefinition("scheduledScannerJob"), is(false));
        assertThat(registry.containsBeanDefinition("oneOffScannerJob"), is(false));
        assertThat(registry.containsBeanDefinition("customRegistryScannerJob"), is(false));
    }
    
    @Test
    void assertScanWithAlreadyRegisteredJobBean() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        // Simulate ordinary component scanning (@Component) having registered the job bean before @ElasticJobScan runs.
        registry.registerBeanDefinition("scannerScheduleJob", BeanDefinitionBuilder.genericBeanDefinition(ScannerScheduleJob.class).getBeanDefinition());
        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        scanner.scan("org.apache.shardingsphere.elasticjob.spring.core.scanner.fixture");
        assertTrue(registry.containsBeanDefinition("scannerScheduleJob"));
        assertTrue(registry.containsBeanDefinition("scheduledScannerJob"));
        BeanDefinition scheduleJobBootstrap = registry.getBeanDefinition("scheduledScannerJob");
        assertThat(scheduleJobBootstrap.getBeanClassName(), is(ScheduleJobBootstrap.class.getName()));
        assertThat(scheduleJobBootstrap.getInitMethodName(), is("schedule"));
        assertThat(scheduleJobBootstrap.getConstructorArgumentValues().getArgumentCount(), is(2));
    }
}
