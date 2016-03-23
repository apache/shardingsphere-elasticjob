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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * 基于Zookeeper注册中心的命名空间解析器.
 * 
 * @author zhangliang
 */
public class ZookeeperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    @Override
    protected Class<?> getBeanClass(final Element element) {
        return SpringZookeeperRegistryCenter.class;
    }
    
    @Override
    protected void doParse(final Element element, final BeanDefinitionBuilder bean) {
        bean.addConstructorArgValue(createZookeeperConfiguration(element));
        bean.setDestroyMethodName("close");
    }
    
    private SpringZookeeperConfigurationDto createZookeeperConfiguration(final Element element) {
        SpringZookeeperConfigurationDto result = new SpringZookeeperConfigurationDto(
                element.getAttribute("serverLists"), 
                element.getAttribute("namespace"), 
                element.getAttribute("baseSleepTimeMilliseconds"), 
                element.getAttribute("maxSleepTimeMilliseconds"), 
                element.getAttribute("maxRetries"));
        result.setSessionTimeoutMilliseconds(element.getAttribute("sessionTimeoutMilliseconds"));
        result.setConnectionTimeoutMilliseconds(element.getAttribute("connectionTimeoutMilliseconds"));
        result.setDigest(element.getAttribute("digest"));
        result.setNestedPort(element.getAttribute("nestedPort"));
        result.setNestedDataDir(element.getAttribute("nestedDataDir"));
        result.setLocalPropertiesPath(element.getAttribute("localPropertiesPath"));
        result.setOverwrite(element.getAttribute("overwrite"));
        return result;
    }
}
