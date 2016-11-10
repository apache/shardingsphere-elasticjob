/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.spring.namespace.parser.common;

import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.spring.schedule.SpringJobScheduler;
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

import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.CRON_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.DESCRIPTION_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.DISABLED_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_TAG;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.EVENT_TRACE_RDB_DATA_SOURCE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.EXECUTOR_SERVICE_HANDLER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.FAILOVER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.JOB_EXCEPTION_HANDLER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.JOB_PARAMETER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.JOB_SHARDING_STRATEGY_CLASS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.LISTENER_TAG;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.MAX_TIME_DIFF_SECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.MISFIRE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.MONITOR_EXECUTION_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.MONITOR_PORT_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.OVERWRITE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.REGISTRY_CENTER_REF_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.SHARDING_ITEM_PARAMETERS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.namespace.constants.BaseJobBeanDefinitionParserTag.SHARDING_TOTAL_COUNT_ATTRIBUTE;

/**
 * 基本作业的命名空间解析器.
 * 
 * @author zhangliang
 * @author caohao
 */
public abstract class AbstractJobBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
        factory.setInitMethodName("init");
        factory.setDestroyMethodName("shutdown");
        factory.addConstructorArgValue(BeanDefinitionBuilder.rootBeanDefinition(element.getAttribute(CLASS_ATTRIBUTE)).getBeanDefinition());
        factory.addConstructorArgReference(element.getAttribute(REGISTRY_CENTER_REF_ATTRIBUTE));
        factory.addConstructorArgValue(createJobConfiguration(element));
        BeanDefinition jobEventConfig = createJobEventConfig(element);
        if (null != jobEventConfig) {
            factory.addConstructorArgValue(jobEventConfig);
        }
        factory.addConstructorArgValue(createJobListeners(element));
        return factory.getBeanDefinition();
    }
    
    protected abstract Class<? extends AbstractJobConfigurationDto> getJobConfigurationDTO();
    
    protected abstract void setPropertiesValue(final Element element, final BeanDefinitionBuilder factory);
    
    private BeanDefinition createJobConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(getJobConfigurationDTO());
        String jobName = element.getAttribute(ID_ATTRIBUTE);
        factory.addConstructorArgValue(jobName);
        factory.addConstructorArgValue(element.getAttribute(CRON_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(SHARDING_TOTAL_COUNT_ATTRIBUTE));
        addPropertyValueIfNotEmpty(SHARDING_ITEM_PARAMETERS_ATTRIBUTE, "shardingItemParameters", element, factory);
        addPropertyValueIfNotEmpty(JOB_PARAMETER_ATTRIBUTE, "jobParameter", element, factory);
        addPropertyValueIfNotEmpty(MONITOR_EXECUTION_ATTRIBUTE, "monitorExecution", element, factory);
        addPropertyValueIfNotEmpty(MONITOR_PORT_ATTRIBUTE, "monitorPort", element, factory);
        addPropertyValueIfNotEmpty(MAX_TIME_DIFF_SECONDS_ATTRIBUTE, "maxTimeDiffSeconds", element, factory);
        addPropertyValueIfNotEmpty(FAILOVER_ATTRIBUTE, "failover", element, factory);
        addPropertyValueIfNotEmpty(MISFIRE_ATTRIBUTE, "misfire", element, factory);
        addPropertyValueIfNotEmpty(JOB_SHARDING_STRATEGY_CLASS_ATTRIBUTE, "jobShardingStrategyClass", element, factory);
        addPropertyValueIfNotEmpty(DESCRIPTION_ATTRIBUTE, "description", element, factory);
        addPropertyValueIfNotEmpty(DISABLED_ATTRIBUTE, "disabled", element, factory);
        addPropertyValueIfNotEmpty(OVERWRITE_ATTRIBUTE, "overwrite", element, factory);
        addPropertyValueIfNotEmpty(EXECUTOR_SERVICE_HANDLER_ATTRIBUTE, "executorServiceHandler", element, factory);
        addPropertyValueIfNotEmpty(JOB_EXCEPTION_HANDLER_ATTRIBUTE, "jobExceptionHandler", element, factory);
        setPropertiesValue(element, factory);
        return factory.getBeanDefinition();
    }
    
    private BeanDefinition createJobEventConfig(final Element element) {
        String eventTraceDataSourceName = element.getAttribute(EVENT_TRACE_RDB_DATA_SOURCE_ATTRIBUTE);
        if (Strings.isNullOrEmpty(eventTraceDataSourceName)) {
            return null;
        }
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
        factory.addConstructorArgReference(eventTraceDataSourceName);
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> createJobListeners(final Element element) {
        Element listenerElement = DomUtils.getChildElementByTagName(element, LISTENER_TAG);
        Element distributedListenerElement = DomUtils.getChildElementByTagName(element, DISTRIBUTED_LISTENER_TAG);
        List<BeanDefinition> result = new ManagedList<>(2);
        if (null != listenerElement) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listenerElement.getAttribute(CLASS_ATTRIBUTE));
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            result.add(factory.getBeanDefinition());
        }
        if (null != distributedListenerElement) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListenerElement.getAttribute(CLASS_ATTRIBUTE));
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            factory.addConstructorArgValue(distributedListenerElement.getAttribute(DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS_ATTRIBUTE));
            factory.addConstructorArgValue(distributedListenerElement.getAttribute(DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS_ATTRIBUTE));
            result.add(factory.getBeanDefinition());
        }
        return result;
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
