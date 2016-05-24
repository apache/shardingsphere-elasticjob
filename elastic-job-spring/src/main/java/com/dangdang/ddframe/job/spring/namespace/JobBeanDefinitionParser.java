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

package com.dangdang.ddframe.job.spring.namespace;

import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.spring.schedule.SpringJobScheduler;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

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
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
        factory.setInitMethodName("init");
        factory.addConstructorArgReference(element.getAttribute("regCenter"));
        factory.addConstructorArgReference(createJobConfiguration(element, parserContext));
        factory.addConstructorArgValue(createJobListeners(element));
        return factory.getBeanDefinition();
    }
    
    private String createJobConfiguration(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition("com.dangdang.ddframe.job.api.JobConfiguration");
        String className = element.getAttribute("class");
        //TODO 增加作业类型,job:dataflow,job:simple,job:script,job:sequence
        if (Strings.isNullOrEmpty(className)) {
            Preconditions.checkNotNull(element.getAttribute("scriptCommandLine"), "Cannot find script command line.");
            className = "com.dangdang.ddframe.job.plugin.job.type.integrated.ScriptElasticJob";
        }
        factory.addConstructorArgValue(element.getAttribute("id"));
        factory.addConstructorArgValue(className);
        factory.addConstructorArgValue(element.getAttribute("shardingTotalCount"));
        factory.addConstructorArgValue(element.getAttribute("cron"));
        addPropertyValueIfNotEmpty("shardingItemParameters", element, factory);
        addPropertyValueIfNotEmpty("jobParameter", element, factory);
        addPropertyValueIfNotEmpty("monitorExecution", element, factory);
        addPropertyValueIfNotEmpty("monitorPort", element, factory);
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
        addPropertyValueIfNotEmpty("scriptCommandLine", element, factory);
        String result = element.getAttribute("id") + "Conf";
        parserContext.getRegistry().registerBeanDefinition(result, factory.getBeanDefinition());
        return result;
    }
    
    private List<BeanDefinition> createJobListeners(final Element element) {
        List<Element> listenerElements = DomUtils.getChildElementsByTagName(element, "listener");
        List<BeanDefinition> result = new ManagedList<>(listenerElements.size());
        for (Element each : listenerElements) {
            String className = each.getAttribute("class");
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(className);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            try {
                Class listenerClass = Class.forName(className);
                if (AbstractDistributeOnceElasticJobListener.class.isAssignableFrom(listenerClass)) {
                    factory.addConstructorArgValue(each.getAttribute("startedTimeoutMilliseconds"));
                    factory.addConstructorArgValue(each.getAttribute("completedTimeoutMilliseconds"));
                }
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            result.add(factory.getBeanDefinition());
        }
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
