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

package org.apache.shardingsphere.elasticjob.spring.namespace.job.parser;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.spring.namespace.job.tag.JobBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Arrays;

/**
 * Job bean definition parser.
 */
public final class JobBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory;
        if (Strings.isNullOrEmpty(element.getAttribute(JobBeanDefinitionTag.CRON_ATTRIBUTE))) {
            factory = BeanDefinitionBuilder.rootBeanDefinition(OneOffJobBootstrap.class);
        } else {
            factory = BeanDefinitionBuilder.rootBeanDefinition(ScheduleJobBootstrap.class);
            factory.setInitMethodName("schedule");
        }
        factory.addConstructorArgReference(element.getAttribute(JobBeanDefinitionTag.REGISTRY_CENTER_REF_ATTRIBUTE));
        String jobType = element.getAttribute(JobBeanDefinitionTag.JOB_TYPE_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(jobType)) {
            factory.addConstructorArgValue(jobType);
        } else {
            factory.addConstructorArgReference(element.getAttribute(JobBeanDefinitionTag.JOB_REF_ATTRIBUTE));
        }
        factory.addConstructorArgValue(createJobConfigurationBeanDefinition(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    /**
     * Create the bean definition of job configuration.
     *
     * <p>The configuration is built by {@link JobConfigurationFactoryBean} from property values only,
     * because Spring AOT cannot process the constructor arguments of {@code JobConfiguration}.</p>
     *
     * @param element job element
     * @param parserContext parser context
     * @return bean definition of job configuration
     */
    private BeanDefinition createJobConfigurationBeanDefinition(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.genericBeanDefinition(JobConfigurationFactoryBean.class);
        result.addPropertyValue("jobName", element.getAttribute(ID_ATTRIBUTE));
        result.addPropertyValue("cron", element.getAttribute(JobBeanDefinitionTag.CRON_ATTRIBUTE));
        result.addPropertyValue("timeZone", element.getAttribute(JobBeanDefinitionTag.TIME_ZONE_ATTRIBUTE));
        result.addPropertyValue("shardingTotalCount", element.getAttribute(JobBeanDefinitionTag.SHARDING_TOTAL_COUNT_ATTRIBUTE));
        result.addPropertyValue("shardingItemParameters", element.getAttribute(JobBeanDefinitionTag.SHARDING_ITEM_PARAMETERS_ATTRIBUTE));
        result.addPropertyValue("jobParameter", element.getAttribute(JobBeanDefinitionTag.JOB_PARAMETER_ATTRIBUTE));
        result.addPropertyValue("monitorExecution", element.getAttribute(JobBeanDefinitionTag.MONITOR_EXECUTION_ATTRIBUTE));
        result.addPropertyValue("failover", element.getAttribute(JobBeanDefinitionTag.FAILOVER_ATTRIBUTE));
        result.addPropertyValue("misfire", element.getAttribute(JobBeanDefinitionTag.MISFIRE_ATTRIBUTE));
        result.addPropertyValue("maxTimeDiffSeconds", element.getAttribute(JobBeanDefinitionTag.MAX_TIME_DIFF_SECONDS_ATTRIBUTE));
        result.addPropertyValue("reconcileIntervalMinutes", element.getAttribute(JobBeanDefinitionTag.RECONCILE_INTERVAL_MINUTES));
        if (element.hasAttribute(JobBeanDefinitionTag.JOB_SHARDING_STRATEGY_TYPE_ATTRIBUTE)) {
            result.addPropertyValue("jobShardingStrategyType", element.getAttribute(JobBeanDefinitionTag.JOB_SHARDING_STRATEGY_TYPE_ATTRIBUTE));
        }
        if (element.hasAttribute(JobBeanDefinitionTag.JOB_EXECUTOR_SERVICE_HANDLER_TYPE_ATTRIBUTE)) {
            result.addPropertyValue("jobExecutorThreadPoolSizeProviderType", element.getAttribute(JobBeanDefinitionTag.JOB_EXECUTOR_SERVICE_HANDLER_TYPE_ATTRIBUTE));
        }
        if (element.hasAttribute(JobBeanDefinitionTag.JOB_ERROR_HANDLER_TYPE_ATTRIBUTE)) {
            result.addPropertyValue("jobErrorHandlerType", element.getAttribute(JobBeanDefinitionTag.JOB_ERROR_HANDLER_TYPE_ATTRIBUTE));
        }
        result.addPropertyValue("jobListenerTypes", parseJobListenerTypes(element));
        result.addPropertyValue("extraConfigurations", parseExtraConfigs(new String[]{JobBeanDefinitionTag.TRACING_REF_ATTRIBUTE}, element));
        result.addPropertyValue("description", element.getAttribute(JobBeanDefinitionTag.DESCRIPTION_ATTRIBUTE));
        Element propsElement = DomUtils.getChildElementByTagName(element, JobBeanDefinitionTag.PROPS_TAG);
        if (null != propsElement) {
            result.addPropertyValue("props", parserContext.getDelegate().parsePropsElement(propsElement));
        }
        result.addPropertyValue("disabled", element.getAttribute(JobBeanDefinitionTag.DISABLED_ATTRIBUTE));
        result.addPropertyValue("overwrite", element.getAttribute(JobBeanDefinitionTag.OVERWRITE_ATTRIBUTE));
        result.addPropertyValue("label", element.getAttribute(JobBeanDefinitionTag.LABEL_ATTRIBUTE));
        result.addPropertyValue("staticSharding", element.getAttribute(JobBeanDefinitionTag.STATIC_SHARDING_ATTRIBUTE));
        result.addPropertyValue("maxWaitMillis", element.getAttribute(JobBeanDefinitionTag.MAX_WAIT_MILLIS_ATTRIBUTE));
        return result.getBeanDefinition();
    }
    
    private ManagedList<String> parseJobListenerTypes(final Element element) {
        ManagedList<String> result = new ManagedList<>();
        String jobListenerTypes = element.getAttribute(JobBeanDefinitionTag.JOB_LISTENER_TYPES_ATTRIBUTE);
        if (!ObjectUtils.isEmpty(jobListenerTypes.trim())) {
            result.addAll(Arrays.asList(jobListenerTypes.split(",")));
        }
        return result;
    }
    
    private ManagedList<RuntimeBeanReference> parseExtraConfigs(final String[] extraConfigRefs, final Element element) {
        ManagedList<RuntimeBeanReference> result = new ManagedList<>(extraConfigRefs.length);
        for (String each : extraConfigRefs) {
            String attribute = element.getAttribute(each);
            if (!Strings.isNullOrEmpty(attribute)) {
                result.add(new RuntimeBeanReference(attribute));
            }
        }
        return result;
    }
}
