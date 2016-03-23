/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.spring.namespace;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.w3c.dom.Element;

import com.dangdang.ddframe.reg.spring.placeholder.RegistryPropertySources;

/**
 * 注册中心配置项使用占位符的命名空间解析器.
 * 
 * @author zhangliang
 */
public class PlaceholderBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    //CHECKSTYLE:OFF
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
    //CHECKSTYLE:ON
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(PropertySourcesPlaceholderConfigurer.class);
        factory.addPropertyValue("ignoreUnresolvablePlaceholders", true);
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(RegistryPropertySources.class);
        definitionBuilder.addConstructorArgReference(element.getAttribute("registerRef"));
        factory.addPropertyValue("propertySources", definitionBuilder.getBeanDefinition());
        return factory.getBeanDefinition();
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
