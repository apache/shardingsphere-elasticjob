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

package com.dangdang.ddframe.reg.spring.placeholder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.dangdang.ddframe.reg.fixture.PlaceholderAnnotationBean;
import com.dangdang.ddframe.reg.fixture.PlaceholderXmlBean;

@ContextConfiguration(locations = "classpath:META-INF/reg/withNamespace.xml")
public class RegPlaceholderSpringIntegrateTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private PlaceholderAnnotationBean placeholderAnnotationBean;
    
    @Resource
    private PlaceholderXmlBean placeholderXmlBean;
    
    @Test
    public void placeholderForAnnotation() {
        assertThat(placeholderAnnotationBean.getRegValue1(), is("deepNested"));
        assertThat(placeholderAnnotationBean.getRegValue2(), is("new"));
        assertThat(placeholderAnnotationBean.getWithNamespaceValue1(), is("with_namespace_value_1"));
        assertThat(placeholderAnnotationBean.getWithNamespaceValue2(), is("with_namespace_value_2"));
        assertThat(placeholderAnnotationBean.getWithoutNamespaceValue1(), is("without_namespace_value_1"));
        assertThat(placeholderAnnotationBean.getWithoutNamespaceValue2(), is("without_namespace_value_2"));
    }
    
    @Test
    public void placeholderForXml() {
        assertThat(placeholderXmlBean.getRegValue1(), is("${/test/deep/nested}"));
        assertThat(placeholderXmlBean.getRegValue2(), is("${/new}"));
        assertThat(placeholderXmlBean.getWithNamespaceValue1(), is("with_namespace_value_1"));
        assertThat(placeholderXmlBean.getWithNamespaceValue2(), is("with_namespace_value_2"));
        assertThat(placeholderXmlBean.getWithoutNamespaceValue1(), is("without_namespace_value_1"));
        assertThat(placeholderXmlBean.getWithoutNamespaceValue2(), is("without_namespace_value_2"));
    }
}
