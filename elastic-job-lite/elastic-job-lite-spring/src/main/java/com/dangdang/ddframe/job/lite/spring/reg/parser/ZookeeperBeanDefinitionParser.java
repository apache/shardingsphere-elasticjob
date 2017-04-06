/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.spring.reg.parser;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Strings;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 基于Zookeeper注册中心的命名空间解析器.
 * 
 * @author caohao
 */
public final class ZookeeperBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperRegistryCenter.class);
        result.addConstructorArgValue(buildZookeeperConfigurationBeanDefinition(element));
        result.setInitMethodName("init");
        return result.getBeanDefinition();
    }
    
    private AbstractBeanDefinition buildZookeeperConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder configuration = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperConfiguration.class);
        configuration.addConstructorArgValue(element.getAttribute("server-lists"));
        configuration.addConstructorArgValue(element.getAttribute("namespace"));
        addPropertyValueIfNotEmpty("base-sleep-time-milliseconds", "baseSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty("max-sleep-time-milliseconds", "maxSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty("max-retries", "maxRetries", element, configuration);
        addPropertyValueIfNotEmpty("session-timeout-milliseconds", "sessionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty("connection-timeout-milliseconds", "connectionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty("digest", "digest", element, configuration);
        return configuration.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}
