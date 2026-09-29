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

import com.google.common.base.Strings;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Abstract bean definition parser of the registry center.
 */
public abstract class AbstractRegistryCenterBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(getRegistryCenterClass());
        result.addConstructorArgValue(buildConfigurationBeanDefinition(element));
        result.setInitMethodName("init");
        result.setDestroyMethodName("close");
        return result.getBeanDefinition();
    }
    
    /**
     * Get the class of the registry center.
     *
     * @return class of the registry center
     */
    protected abstract Class<?> getRegistryCenterClass();
    
    /**
     * Build the bean definition of the registry center configuration.
     *
     * @param element element of the bean definition
     * @return bean definition of the registry center configuration
     */
    protected abstract AbstractBeanDefinition buildConfigurationBeanDefinition(Element element);
    
    /**
     * Add the property if the attribute value is not empty.
     *
     * @param attributeName attribute name of the element
     * @param propertyName property name of the configuration
     * @param element element of the bean definition
     * @param factory bean definition builder of the configuration
     */
    protected final void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}
