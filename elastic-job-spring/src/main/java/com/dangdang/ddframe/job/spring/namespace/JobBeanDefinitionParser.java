/**
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

package com.dangdang.ddframe.job.spring.namespace;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.spring.schedule.SpringJobController;
import com.google.common.base.Strings;

/**
 * 分布式作业的命名空间解析器.
 * 
 * @author zhangliang
 */
public class JobBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    //CHECKSTYLE:OFF
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
    //CHECKSTYLE:ON
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobController.class);
        factory.setInitMethodName("init");
        factory.setDestroyMethodName("shutdown");
        factory.addConstructorArgReference(element.getAttribute("regCenter"));
        factory.addConstructorArgReference(createJobConfiguration(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private String createJobConfiguration(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JobConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute("id"));
        factory.addConstructorArgValue(element.getAttribute("class"));
        factory.addConstructorArgValue(element.getAttribute("shardingTotalCount"));
        factory.addConstructorArgValue(element.getAttribute("cron"));
        addPropertyValueIfNotEmpty("shardingItemParameters", element, factory);
        addPropertyValueIfNotEmpty("jobParameter", element, factory);
        addPropertyValueIfNotEmpty("monitorExecution", element, factory);
        addPropertyValueIfNotEmpty("processCountIntervalSeconds", element, factory);
        addPropertyValueIfNotEmpty("concurrentDataProcessThreadCount", element, factory);
        addPropertyValueIfNotEmpty("fetchDataCount", element, factory);
        addPropertyValueIfNotEmpty("maxTimeDiffSeconds", element, factory);
        addPropertyValueIfNotEmpty("failover", element, factory);
        addPropertyValueIfNotEmpty("misfire", element, factory);
        addPropertyValueIfNotEmpty("jobShardingStrategyClass", element, factory);
        addPropertyValueIfNotEmpty("description", element, factory);
        addPropertyValueIfNotEmpty("disabled", element, factory);
        addPropertyValueIfNotEmpty("overwrite", element, factory);
        String result = element.getAttribute("id") + "Conf";
        parserContext.getRegistry().registerBeanDefinition(result, factory.getBeanDefinition());
        return result;
    }
    
    private void addPropertyValueIfNotEmpty(final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String propertyValue = element.getAttribute(propertyName);
        if (!Strings.isNullOrEmpty(propertyValue)) {
            factory.addPropertyValue(propertyName, propertyValue);
        }
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
