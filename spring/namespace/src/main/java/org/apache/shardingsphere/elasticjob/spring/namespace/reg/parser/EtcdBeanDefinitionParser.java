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

import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.namespace.reg.tag.EtcdBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Bean definition parser for etcd.
 */
public final class EtcdBeanDefinitionParser extends AbstractRegistryCenterBeanDefinitionParser {
    
    @Override
    protected Class<?> getRegistryCenterClass() {
        return EtcdRegistryCenter.class;
    }
    
    @Override
    protected AbstractBeanDefinition buildConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder config = BeanDefinitionBuilder.rootBeanDefinition(EtcdConfiguration.class);
        config.addConstructorArgValue(element.getAttribute(EtcdBeanDefinitionTag.SERVER_LISTS_ATTRIBUTE));
        config.addConstructorArgValue(element.getAttribute(EtcdBeanDefinitionTag.NAMESPACE_ATTRIBUTE));
        addPropertyValueIfNotEmpty(EtcdBeanDefinitionTag.CONNECTION_TIMEOUT_MILLISECONDS_ATTRIBUTE, "connectionTimeoutMilliseconds", element, config);
        addPropertyValueIfNotEmpty(EtcdBeanDefinitionTag.USERNAME_ATTRIBUTE, "username", element, config);
        addPropertyValueIfNotEmpty(EtcdBeanDefinitionTag.PASSWORD_ATTRIBUTE, "password", element, config);
        addPropertyValueIfNotEmpty(EtcdBeanDefinitionTag.SSL_ATTRIBUTE, "ssl", element, config);
        addPropertyValueIfNotEmpty(EtcdBeanDefinitionTag.AUTHORITY_ATTRIBUTE, "authority", element, config);
        return config.getBeanDefinition();
    }
}
