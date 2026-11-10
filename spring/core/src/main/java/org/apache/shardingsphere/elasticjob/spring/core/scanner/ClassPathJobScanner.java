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
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Objects;
import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that registers job bootstraps by {@code basePackage}.
 *
 * <p>
 * Schedule jobs (with {@code cron}) are registered as {@link ScheduleJobBootstrap} with init method {@code schedule}.
 * One-off jobs (without {@code cron}) are registered as {@link OneOffJobBootstrap} without init method.
 * Only bean references are registered in the bean definition so that Spring AOT native images can generate code for them.
 * Job configuration is resolved at runtime from {@link ElasticJobConfiguration} (with superclass fallback for CGLIB proxies).
 * Registration is idempotent: a bootstrap whose {@code jobName} is already taken (e.g. equal to the scanned job bean name,
 * or already registered by AOT generated code at native runtime) is skipped. Therefore the {@code jobName} must differ
 * from the scanned job bean name.
 * Beans already registered (e.g. via {@code @Component} with ordinary component scanning) are reused:
 * the scanner still registers their bootstraps instead of skipping them.
 * </p>
 *
 * @see ScheduleJobBootstrap
 * @see OneOffJobBootstrap
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
        processAlreadyRegisteredBeans(basePackages);
        return beanDefinitions;
    }
    
    private void processBeanDefinitions(final Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            ScannedGenericBeanDefinition definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();
            Class<?> jobClass;
            try {
                jobClass = Class.forName(definition.getMetadata().getClassName());
            } catch (final ClassNotFoundException ex) {
                // TODO： log
                continue;
            }
            ElasticJobConfiguration jobAnnotation = findAnnotation(jobClass);
            if (null == jobAnnotation) {
                continue;
            }
            registerBootstrap(holder.getBeanName(), jobAnnotation);
        }
    }
    
    /**
     * Register bootstraps for job beans already present in the registry.
     *
     * <p>
     * With ordinary Spring Boot component scanning, a job class annotated with {@code @Component}
     * is registered before {@link JobScannerConfiguration} runs. {@code super.doScan} then skips it
     * via {@code checkCandidate}, so no bootstrap would be created. This pass reuses the existing
     * job bean name and registers the missing bootstrap.
     * </p>
     *
     * @param basePackages the packages to check for annotated classes
     */
    private void processAlreadyRegisteredBeans(final String... basePackages) {
        BeanDefinitionRegistry registry = getRegistry();
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String className = beanDefinition.getBeanClassName();
            if (null == className && beanDefinition instanceof AbstractBeanDefinition
                    && ((AbstractBeanDefinition) beanDefinition).hasBeanClass()) {
                className = ((AbstractBeanDefinition) beanDefinition).getBeanClass().getName();
            }
            if (null == className || !isInBasePackages(className, basePackages)) {
                continue;
            }
            // Skip bootstraps registered above. They reference job beans and their class has no job annotation.
            if (OneOffJobBootstrap.class.getName().equals(className) || ScheduleJobBootstrap.class.getName().equals(className)) {
                continue;
            }
            Class<?> jobClass;
            try {
                jobClass = Class.forName(className);
            } catch (final ClassNotFoundException ex) {
                // TODO： log
                continue;
            }
            ElasticJobConfiguration jobAnnotation = findAnnotation(jobClass);
            if (null == jobAnnotation) {
                continue;
            }
            if (registry.containsBeanDefinition(jobAnnotation.jobName())) {
                continue;
            }
            registerBootstrap(beanName, jobAnnotation);
        }
    }
    
    private void registerBootstrap(final String jobBeanName, final ElasticJobConfiguration jobAnnotation) {
        String registryCenter = jobAnnotation.registryCenter();
        BeanDefinitionBuilder factory;
        if (StringUtils.isEmpty(jobAnnotation.cron())) {
            factory = BeanDefinitionBuilder.rootBeanDefinition(OneOffJobBootstrap.class);
        } else {
            factory = BeanDefinitionBuilder.rootBeanDefinition(ScheduleJobBootstrap.class);
            factory.setInitMethodName("schedule");
        }
        if (!StringUtils.isEmpty(registryCenter)) {
            factory.addConstructorArgReference(registryCenter);
        } else {
            factory.addConstructorArgValue(new RuntimeBeanReference(CoordinatorRegistryCenter.class));
        }
        factory.addConstructorArgReference(Objects.requireNonNull(jobBeanName));
        BeanDefinitionRegistry registry = getRegistry();
        if (!registry.containsBeanDefinition(jobAnnotation.jobName())) {
            registry.registerBeanDefinition(jobAnnotation.jobName(), factory.getBeanDefinition());
        }
    }
    
    private static ElasticJobConfiguration findAnnotation(final Class<?> jobClass) {
        ElasticJobConfiguration result = jobClass.getAnnotation(ElasticJobConfiguration.class);
        if (null != result) {
            return result;
        }
        Class<?> current = jobClass.getSuperclass();
        while (null != current && Object.class != current) {
            result = current.getAnnotation(ElasticJobConfiguration.class);
            if (null != result) {
                return result;
            }
            current = current.getSuperclass();
        }
        return null;
    }
    
    private static boolean isInBasePackages(final String className, final String... basePackages) {
        for (String basePackage : basePackages) {
            if (null != basePackage && !basePackage.isEmpty() && className.startsWith(basePackage + ".")) {
                return true;
            }
        }
        return false;
    }
}
