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
class ZookeeperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
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
                element.getAttribute("server-lists"), 
                element.getAttribute("namespace"), 
                element.getAttribute("base-sleep-time-milliseconds"), 
                element.getAttribute("max-sleep-time-milliseconds"), 
                element.getAttribute("max-retries"));
        result.setSessionTimeoutMilliseconds(element.getAttribute("session-timeout-milliseconds"));
        result.setConnectionTimeoutMilliseconds(element.getAttribute("connection-timeout-milliseconds"));
        result.setDigest(element.getAttribute("digest"));
        result.setNestedPort(element.getAttribute("nested-port"));
        result.setNestedDataDir(element.getAttribute("nested-data-dir"));
        result.setLocalPropertiesPath(element.getAttribute("local-properties-path"));
        result.setOverwrite(element.getAttribute("overwrite"));
        return result;
    }
}
