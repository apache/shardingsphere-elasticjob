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

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.engine.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Objects;
import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that registers ScheduleJobBootstrap by {@code basePackage}.
 *
 * @see ScheduleJobBootstrap
 */
public class ClassPathJobScanner extends ClassPathBeanDefinitionScanner {
    
    public ClassPathJobScanner(final BeanDefinitionRegistry registry) {
        super(registry, false);
    }
    
    /**
     * Calls the parent search that will search and register all the candidates by {@code ElasticJobConfiguration}.
     *
     * @param basePackages the packages to check for annotated classes
     */
    @Override
    protected Set<BeanDefinitionHolder> doScan(final String... basePackages) {
        addIncludeFilter(new AnnotationTypeFilter(ElasticJobConfiguration.class));
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }
    
    private void processBeanDefinitions(final Set<BeanDefinitionHolder> beanDefinitions) {
        BeanDefinitionRegistry registry = getRegistry();
        for (BeanDefinitionHolder holder : beanDefinitions) {
            ScannedGenericBeanDefinition definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();
            Class<?> jobClass;
            try {
                jobClass = Class.forName(definition.getMetadata().getClassName());
            } catch (ClassNotFoundException ex) {
                // TODO： log
                continue;
            }
            ElasticJobConfiguration jobAnnotation = jobClass.getAnnotation(ElasticJobConfiguration.class);
            String registryCenter = jobAnnotation.registryCenter();
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ScheduleJobBootstrap.class);
            factory.setInitMethodName("schedule");
            if (!StringUtils.isEmpty(registryCenter)) {
                factory.addConstructorArgReference(registryCenter);
            } else {
                factory.addConstructorArgValue(new RuntimeBeanReference(CoordinatorRegistryCenter.class));
            }
            factory.addConstructorArgReference(Objects.requireNonNull(holder.getBeanName()));
            registry.registerBeanDefinition(jobAnnotation.jobName(), factory.getBeanDefinition());
        }
    }
}
