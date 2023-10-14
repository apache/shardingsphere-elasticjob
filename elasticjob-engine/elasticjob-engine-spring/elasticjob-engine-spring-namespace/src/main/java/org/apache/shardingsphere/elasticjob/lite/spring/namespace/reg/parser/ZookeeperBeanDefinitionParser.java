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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.reg.parser;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.reg.tag.ZookeeperBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for ZooKeeper.
 */
public final class ZookeeperBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperRegistryCenter.class);
        result.addConstructorArgValue(buildZookeeperConfigurationBeanDefinition(element));
        result.setInitMethodName("init");
        result.setDestroyMethodName("close");
        return result.getBeanDefinition();
    }
    
    private AbstractBeanDefinition buildZookeeperConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder configuration = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperConfiguration.class);
        configuration.addConstructorArgValue(element.getAttribute(ZookeeperBeanDefinitionTag.SERVER_LISTS_ATTRIBUTE));
        configuration.addConstructorArgValue(element.getAttribute(ZookeeperBeanDefinitionTag.NAMESPACE_ATTRIBUTE));
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.BASE_SLEEP_TIME_MILLISECONDS_ATTRIBUTE, "baseSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.MAX_SLEEP_TIME_MILLISECONDS_ATTRIBUTE, "maxSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.MAX_RETRIES_ATTRIBUTE, "maxRetries", element, configuration);
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.SESSION_TIMEOUT_MILLISECONDS_ATTRIBUTE, "sessionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.CONNECTION_TIMEOUT_MILLISECONDS_ATTRIBUTE, "connectionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(ZookeeperBeanDefinitionTag.DIGEST_ATTRIBUTE, "digest", element, configuration);
        return configuration.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}
