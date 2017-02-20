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

package com.dangdang.ddframe.job.lite.spring.job.parser.common;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.google.common.base.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.CRON_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.DESCRIPTION_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.DISABLED_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_TAG;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.EVENT_TRACE_RDB_DATA_SOURCE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.EXECUTOR_SERVICE_HANDLER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.FAILOVER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.JOB_EXCEPTION_HANDLER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.JOB_PARAMETER_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.JOB_SHARDING_STRATEGY_CLASS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.LISTENER_TAG;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.MAX_TIME_DIFF_SECONDS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.MISFIRE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.MONITOR_EXECUTION_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.MONITOR_PORT_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.OVERWRITE_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.REGISTRY_CENTER_REF_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.SHARDING_ITEM_PARAMETERS_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.SHARDING_TOTAL_COUNT_ATTRIBUTE;
import static com.dangdang.ddframe.job.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag.RECONCILE_INTERVAL_MINUTES;

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
        if ("".equals(element.getAttribute(CLASS_ATTRIBUTE))) {
            factory.addConstructorArgValue(null);
        } else {
            factory.addConstructorArgValue(BeanDefinitionBuilder.rootBeanDefinition(element.getAttribute(CLASS_ATTRIBUTE)).getBeanDefinition());
        }
        factory.addConstructorArgReference(element.getAttribute(REGISTRY_CENTER_REF_ATTRIBUTE));
        factory.addConstructorArgValue(createLiteJobConfiguration(element));
        BeanDefinition jobEventConfig = createJobEventConfig(element);
        if (null != jobEventConfig) {
            factory.addConstructorArgValue(jobEventConfig);
        }
        factory.addConstructorArgValue(createJobListeners(element));
        return factory.getBeanDefinition();
    }
    
    protected abstract BeanDefinition getJobTypeConfigurationBeanDefinition(final BeanDefinition jobCoreConfigurationBeanDefinition, final Element element);
    
    private BeanDefinition createLiteJobConfiguration(final Element element) {
        return createLiteJobConfigurationBeanDefinition(element, createJobCoreBeanDefinition(element));
    }
    
    private BeanDefinition createLiteJobConfigurationBeanDefinition(final Element element, final BeanDefinition jobCoreBeanDefinition) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(LiteJobConfiguration.class);
        result.addConstructorArgValue(getJobTypeConfigurationBeanDefinition(jobCoreBeanDefinition, element));
        result.addConstructorArgValue(element.getAttribute(MONITOR_EXECUTION_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(MAX_TIME_DIFF_SECONDS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(MONITOR_PORT_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JOB_SHARDING_STRATEGY_CLASS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(DISABLED_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(OVERWRITE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(RECONCILE_INTERVAL_MINUTES));
        return result.getBeanDefinition();
    }
    
    private BeanDefinition createJobCoreBeanDefinition(final Element element) {
        BeanDefinitionBuilder jobCoreBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(JobCoreConfiguration.class);
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(CRON_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(SHARDING_TOTAL_COUNT_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(SHARDING_ITEM_PARAMETERS_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(JOB_PARAMETER_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(FAILOVER_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(MISFIRE_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(element.getAttribute(DESCRIPTION_ATTRIBUTE));
        jobCoreBeanDefinitionBuilder.addConstructorArgValue(createJobPropertiesBeanDefinition(element));
        return jobCoreBeanDefinitionBuilder.getBeanDefinition();
    }
    
    private BeanDefinition createJobPropertiesBeanDefinition(final Element element) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(JobProperties.class);
        Map<JobPropertiesEnum, String> map = new LinkedHashMap<>(JobPropertiesEnum.values().length, 1);
        map.put(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER, element.getAttribute(EXECUTOR_SERVICE_HANDLER_ATTRIBUTE));
        map.put(JobPropertiesEnum.JOB_EXCEPTION_HANDLER, element.getAttribute(JOB_EXCEPTION_HANDLER_ATTRIBUTE));
        result.addConstructorArgValue(map);
        return result.getBeanDefinition();
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
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
