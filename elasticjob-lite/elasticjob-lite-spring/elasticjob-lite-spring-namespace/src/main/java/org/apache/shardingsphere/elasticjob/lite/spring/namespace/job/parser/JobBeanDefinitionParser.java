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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.job.parser;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.job.tag.JobBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

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
    
    private BeanDefinition createJobConfigurationBeanDefinition(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(JobConfiguration.class);
        result.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.CRON_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.SHARDING_TOTAL_COUNT_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.SHARDING_ITEM_PARAMETERS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.JOB_PARAMETER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.MONITOR_EXECUTION_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.FAILOVER_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.MISFIRE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.MAX_TIME_DIFF_SECONDS_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.RECONCILE_INTERVAL_MINUTES));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.JOB_SHARDING_STRATEGY_TYPE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.JOB_EXECUTOR_SERVICE_HANDLER_TYPE_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.JOB_ERROR_HANDLER_TYPE_ATTRIBUTE));
        if (StringUtils.isEmpty(element.getAttribute(JobBeanDefinitionTag.JOB_LISTENER_TYPES_ATTRIBUTE).trim())) {
            result.addConstructorArgValue(Collections.emptyList());
        } else {
            result.addConstructorArgValue(Arrays.asList(element.getAttribute(JobBeanDefinitionTag.JOB_LISTENER_TYPES_ATTRIBUTE).split(",")));
        }
        result.addConstructorArgValue(parseExtraConfigs(new String[]{JobBeanDefinitionTag.TRACING_REF_ATTRIBUTE},
                element, parserContext));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.DESCRIPTION_ATTRIBUTE));
        result.addConstructorArgValue(parsePropsElement(element, parserContext));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.DISABLED_ATTRIBUTE));
        result.addConstructorArgValue(element.getAttribute(JobBeanDefinitionTag.OVERWRITE_ATTRIBUTE));
        return result.getBeanDefinition();
    }
    
    private Collection<BeanDefinition> parseExtraConfigs(final String[] extraConfigRefs, final Element element, final ParserContext parserContext) {
        Collection<BeanDefinition> result = new ManagedList<>(extraConfigRefs.length);
        for (String each : extraConfigRefs) {
            String attribute = element.getAttribute(each);
            if (!Strings.isNullOrEmpty(attribute)) {
                result.add(parserContext.getRegistry().getBeanDefinition(attribute));
            }
        }
        return result;
    }
    
    private Properties parsePropsElement(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, JobBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
