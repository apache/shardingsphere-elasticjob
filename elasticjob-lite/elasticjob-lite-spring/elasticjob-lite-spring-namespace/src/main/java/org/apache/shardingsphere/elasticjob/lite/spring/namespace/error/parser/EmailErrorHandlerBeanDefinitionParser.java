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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.error.parser;

import org.apache.shardingsphere.elasticjob.error.handler.email.EmailConfiguration;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.error.tag.EmailErrorHandlerBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Email error handler bean definition parser.
 */
public final class EmailErrorHandlerBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EmailConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.HOST));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.PORT));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.USERNAME));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.PASSWORD));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.USE_SSL));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.SUBJECT));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.FROM));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.TO));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.CC));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.BCC));
        factory.addConstructorArgValue(element.getAttribute(EmailErrorHandlerBeanDefinitionTag.DEBUG));
        return factory.getBeanDefinition();
    }
}
