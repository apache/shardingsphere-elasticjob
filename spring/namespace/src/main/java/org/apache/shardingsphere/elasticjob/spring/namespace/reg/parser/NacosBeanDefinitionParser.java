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

package org.apache.shardingsphere.elasticjob.spring.namespace.reg.parser;

import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.namespace.reg.tag.NacosBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Bean definition parser for Nacos.
 */
public final class NacosBeanDefinitionParser extends AbstractRegistryCenterBeanDefinitionParser {
    
    @Override
    protected Class<?> getRegistryCenterClass() {
        return NacosRegistryCenter.class;
    }
    
    @Override
    protected AbstractBeanDefinition buildConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder config = BeanDefinitionBuilder.rootBeanDefinition(NacosConfiguration.class);
        config.addConstructorArgValue(element.getAttribute(NacosBeanDefinitionTag.SERVER_LISTS_ATTRIBUTE));
        config.addConstructorArgValue(element.getAttribute(NacosBeanDefinitionTag.NAMESPACE_ATTRIBUTE));
        addPropertyValueIfNotEmpty(NacosBeanDefinitionTag.TENANT_ATTRIBUTE, "tenant", element, config);
        addPropertyValueIfNotEmpty(NacosBeanDefinitionTag.USERNAME_ATTRIBUTE, "username", element, config);
        addPropertyValueIfNotEmpty(NacosBeanDefinitionTag.PASSWORD_ATTRIBUTE, "password", element, config);
        addPropertyValueIfNotEmpty(NacosBeanDefinitionTag.TIMEOUT_MS_ATTRIBUTE, "timeoutMs", element, config);
        return config.getBeanDefinition();
    }
}
