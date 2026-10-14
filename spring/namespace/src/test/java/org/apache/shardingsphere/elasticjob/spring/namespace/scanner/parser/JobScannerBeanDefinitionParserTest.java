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

package org.apache.shardingsphere.elasticjob.spring.namespace.scanner.parser;

import org.apache.shardingsphere.elasticjob.spring.core.scanner.JobScannerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobScannerBeanDefinitionParserTest {
    
    @Test
    void assertParseSingleBasePackage() throws Exception {
        Element element = mock(Element.class);
        when(element.getAttribute("base-package")).thenReturn("org.example.fixture");
        AbstractBeanDefinition definition = invokeParseInternal(element);
        assertThat(definition.getBeanClassName(), is(JobScannerConfiguration.class.getName()));
        Object constructorArg = definition.getConstructorArgumentValues().getIndexedArgumentValues().get(0).getValue();
        String[] basePackages = (String[]) constructorArg;
        assertThat(basePackages.length, is(1));
        assertThat(basePackages[0], is("org.example.fixture"));
    }
    
    @Test
    void assertParseMultipleBasePackages() throws Exception {
        Element element = mock(Element.class);
        when(element.getAttribute("base-package")).thenReturn("org.example.one, org.example.two");
        AbstractBeanDefinition definition = invokeParseInternal(element);
        assertThat(definition.getBeanClassName(), is(JobScannerConfiguration.class.getName()));
        Object constructorArg = definition.getConstructorArgumentValues().getIndexedArgumentValues().get(0).getValue();
        String[] basePackages = (String[]) constructorArg;
        assertThat(basePackages.length, is(2));
        assertThat(basePackages[0], is("org.example.one"));
        assertThat(basePackages[1], is("org.example.two"));
    }
    
    @Test
    void assertBuilderAcceptsStringArray() {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JobScannerConfiguration.class);
        factory.addConstructorArgValue(new String[]{"org.example"});
        assertThat(factory.getBeanDefinition().getBeanClassName(), is(JobScannerConfiguration.class.getName()));
    }
    
    private AbstractBeanDefinition invokeParseInternal(final Element element) throws Exception {
        JobScannerBeanDefinitionParser parser = new JobScannerBeanDefinitionParser();
        Method method = JobScannerBeanDefinitionParser.class.getDeclaredMethod("parseInternal", Element.class, org.springframework.beans.factory.xml.ParserContext.class);
        method.setAccessible(true);
        return (AbstractBeanDefinition) method.invoke(parser, element, mock(org.springframework.beans.factory.xml.ParserContext.class));
    }
}
