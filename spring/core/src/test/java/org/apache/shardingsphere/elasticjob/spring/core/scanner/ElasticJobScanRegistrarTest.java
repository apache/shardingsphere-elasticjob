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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticJobScanRegistrarTest {
    
    @Test
    void assertElasticJobScanDefaultValueIsEmpty() throws NoSuchMethodException {
        String[] valueDefault = (String[]) ElasticJobScan.class.getMethod("value").getDefaultValue();
        String[] basePackagesDefault = (String[]) ElasticJobScan.class.getMethod("basePackages").getDefaultValue();
        assertTrue(0 == valueDefault.length);
        assertTrue(0 == basePackagesDefault.length);
    }
    
    @Test
    void assertElasticJobScanIsRuntimeVisibleForNativeIntrospection() {
        assertThat(ElasticJobScan.class.getAnnotation(java.lang.annotation.Documented.class) != null, is(true));
        assertThat(ElasticJobScan.class.getAnnotation(Retention.class).value(), is(RetentionPolicy.RUNTIME));
        assertNotNull(ElasticJobScan.class.getAnnotation(Import.class));
        assertThat(ElasticJobScan.class.getAnnotation(Import.class).value().length, is(1));
        assertThat(ElasticJobScan.class.getAnnotation(Import.class).value()[0].getName(), is(ElasticJobScanRegistrar.class.getName()));
        assertDoesNotThrow(() -> Class.forName(ElasticJobScanRegistrar.class.getName()));
    }
    
    @Test
    void assertRegisterBeanDefinitionsWithValue() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ElasticJobScanRegistrar registrar = new ElasticJobScanRegistrar();
        registrar.registerBeanDefinitions(new StandardAnnotationMetadata(TestValueConfig.class), registry);
        String beanName = TestValueConfig.class.getName() + "#" + ElasticJobScanRegistrar.class.getSimpleName();
        assertTrue(registry.containsBeanDefinition(beanName));
        BeanDefinition definition = registry.getBeanDefinition(beanName);
        assertThat(definition.getBeanClassName(), is(JobScannerConfiguration.class.getName()));
    }
    
    @Test
    void assertRegisterBeanDefinitionsWithBasePackages() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ElasticJobScanRegistrar registrar = new ElasticJobScanRegistrar();
        registrar.registerBeanDefinitions(new StandardAnnotationMetadata(TestBasePackagesConfig.class), registry);
        String beanName = TestBasePackagesConfig.class.getName() + "#" + ElasticJobScanRegistrar.class.getSimpleName();
        assertTrue(registry.containsBeanDefinition(beanName));
    }
    
    @Configuration
    @ElasticJobScan("org.example.fixture")
    static class TestValueConfig {
    }
    
    @Configuration
    @ElasticJobScan(basePackages = {"org.example.one", "org.example.two"})
    static class TestBasePackagesConfig {
    }
}
