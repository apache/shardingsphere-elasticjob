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

package org.apache.shardingsphere.elasticjob.lite.spring.job.parser.common;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.api.JobScheduler;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.dag.JobDagConfig;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Properties;

/**
 * Job bean definition parser.
 */
public abstract class AbstractJobBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @SneakyThrows
    @Override
    protected final AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JobScheduler.class);
        factory.setInitMethodName("init");
        factory.addConstructorArgReference(element.getAttribute(BaseJobBeanDefinitionParserTag.REGISTRY_CENTER_REF_ATTRIBUTE));
        factory.addConstructorArgReference(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_REF_ATTRIBUTE));
        factory.addConstructorArgValue(createJobConfiguration(element));
        BeanDefinition tracingConfig = createTracingConfiguration(element);
        if (null != tracingConfig) {
            factory.addConstructorArgValue(tracingConfig);
        }
        factory.addConstructorArgValue(createJobListeners(element));
        return factory.getBeanDefinition();
    }
    
    private BeanDefinition createJobConfiguration(final Element element) {
        return createJobConfigurationBeanDefinition(element);
    }
    
    private BeanDefinition createJobConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(JobConfiguration.class);
        result.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        result.addConstructorArgValue(getJobType());
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.CRON_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.SHARDING_TOTAL_COUNT_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.SHARDING_ITEM_PARAMETERS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_PARAMETER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.MONITOR_EXECUTION_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.FAILOVER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.MISFIRE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.MAX_TIME_DIFF_SECONDS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.RECONCILE_INTERVAL_MINUTES));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.MONITOR_PORT_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_SHARDING_STRATEGY_TYPE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_EXECUTOR_SERVICE_HANDLER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_ERROR_HANDLER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DESCRIPTION_ATTRIBUTE));
        result.addConstructorArgValue(getProps(element));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DISABLED_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.OVERWRITE_ATTRIBUTE));
        result.addConstructorArgValue(createJobDagConfig(element));
        return result.getBeanDefinition();
    }
    
    private BeanDefinition createTracingConfiguration(final Element element) {
        String eventTraceDataSourceName = element.getAttribute(BaseJobBeanDefinitionParserTag.EVENT_TRACE_RDB_DATA_SOURCE_ATTRIBUTE);
        if (Strings.isNullOrEmpty(eventTraceDataSourceName)) {
            return null;
        }
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TracingConfiguration.class);
        factory.addConstructorArgValue("RDB");
        factory.addConstructorArgReference(eventTraceDataSourceName);
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> createJobListeners(final Element element) {
        Element listenerElement = DomUtils.getChildElementByTagName(element, BaseJobBeanDefinitionParserTag.LISTENER_TAG);
        Element distributedListenerElement = DomUtils.getChildElementByTagName(element, BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_TAG);
        List<BeanDefinition> result = new ManagedList<>(2);
        if (null != listenerElement) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listenerElement.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE));
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            result.add(factory.getBeanDefinition());
        }
        if (null != distributedListenerElement) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListenerElement.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE));
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            factory.addConstructorArgValue(distributedListenerElement.getAttribute(BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS_ATTRIBUTE));
            factory.addConstructorArgValue(distributedListenerElement.getAttribute(BaseJobBeanDefinitionParserTag.DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS_ATTRIBUTE));
            result.add(factory.getBeanDefinition());
        }
        return result;
    }

    private BeanDefinition createJobDagConfig(final Element element) {
        BeanDefinitionBuilder jobDagConfig = BeanDefinitionBuilder.rootBeanDefinition(JobDagConfig.class);
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_GROUP));
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_DEPENDENCIES));
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_RETRY_TIMES));
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_RETRY_INTERVAL));
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_RUN_ALONE));
        jobDagConfig.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.DAG_SKIP_WHEN_FAIL));
        return jobDagConfig.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    protected abstract Properties getProps(Element element);
    
    protected abstract JobType getJobType();
}
