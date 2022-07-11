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

package org.apache.shardingsphere.elasticjob.lite.spring.core.scanner;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link ImportBeanDefinitionRegistrar} to allow annotation configuration of Elastic Job scanning.
 *
 * @see ClassPathJobScanner
 */
public class ElasticJobScanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    /**
     * set resourceLoader.
     * @param resourceLoader resourceLoader
     *
     * @deprecated  NOP
     */
    @Override
    @Deprecated
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        // NOP
    }

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata importingClassMetadata,
                                        final BeanDefinitionRegistry registry) {
        AnnotationAttributes elasticJobScanAttrs =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(ElasticJobScan.class.getName()));
        if (elasticJobScanAttrs != null) {
            registerBeanDefinitions(importingClassMetadata, elasticJobScanAttrs, registry);
        }
    }

    private void registerBeanDefinitions(final AnnotationMetadata annoMeta, final AnnotationAttributes annoAttrs,
            final BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JobScannerConfiguration.class);

        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText)
                .collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText)
                .collect(Collectors.toList()));
        factory.addConstructorArgValue(basePackages);
        registry.registerBeanDefinition(generateBaseBeanName(annoMeta), factory.getBeanDefinition());
    }

    private static String generateBaseBeanName(final AnnotationMetadata importingClassMetadata) {
        return importingClassMetadata.getClassName() + "#" + ElasticJobScanRegistrar.class.getSimpleName();
    }

}
