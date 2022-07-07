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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.scanner;

import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

public class ClassPathJobScanner extends ClassPathBeanDefinitionScanner {

    private Class<? extends Annotation> annotationClass;

    public ClassPathJobScanner(final BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * 设置.
     * @param annotationClass annotationClass
     */
    public void setAnnotationClass(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * 扫描注解，并注入系统.
     * @param basePackages basePackages
     * @return
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(final String... basePackages) {
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
        }

        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(final Set<BeanDefinitionHolder> beanDefinitions) {
        //关键部分，注入实体
        BeanDefinitionRegistry registry = getRegistry();

        for (BeanDefinitionHolder holder : beanDefinitions) {
            ScannedGenericBeanDefinition definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();
            Class<?> jobClass;
            try {
                jobClass = Class.forName(definition.getMetadata().getClassName());
            } catch (ClassNotFoundException ex) {
                //TODO： log
                continue;
            }
            ElasticJobConfiguration jobAnnotation = jobClass.getAnnotation(ElasticJobConfiguration.class);
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ScheduleJobBootstrap.class);
            factory.setInitMethodName("schedule");
            factory.addConstructorArgReference(jobAnnotation.registryCenter());
            factory.addConstructorArgReference(Objects.requireNonNull(holder.getBeanName()));
            registry.registerBeanDefinition(jobAnnotation.jobName(), factory.getBeanDefinition());
        }
    }

}
