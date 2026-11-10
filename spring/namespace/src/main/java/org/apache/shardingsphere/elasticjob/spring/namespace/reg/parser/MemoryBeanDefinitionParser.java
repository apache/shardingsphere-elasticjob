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

import org.apache.shardingsphere.elasticjob.reg.memory.MemoryConfiguration;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.namespace.reg.tag.MemoryBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Bean definition parser for memory.
 */
public final class MemoryBeanDefinitionParser extends AbstractRegistryCenterBeanDefinitionParser {
    
    @Override
    protected Class<?> getRegistryCenterClass() {
        return MemoryRegistryCenter.class;
    }
    
    @Override
    protected AbstractBeanDefinition buildConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder config = BeanDefinitionBuilder.rootBeanDefinition(MemoryConfiguration.class);
        config.addConstructorArgValue(element.getAttribute(MemoryBeanDefinitionTag.NAMESPACE_ATTRIBUTE));
        return config.getBeanDefinition();
    }
}
