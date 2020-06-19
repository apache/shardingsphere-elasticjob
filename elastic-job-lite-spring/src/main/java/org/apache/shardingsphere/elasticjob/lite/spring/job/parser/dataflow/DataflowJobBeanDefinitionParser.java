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

package org.apache.shardingsphere.elasticjob.lite.spring.job.parser.dataflow;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.lite.scheduler.InstanceProvidedDataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.spring.job.parser.common.AbstractJobBeanDefinitionParser;
import org.apache.shardingsphere.elasticjob.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Dataflow job bean definition parser.
 */
public final class DataflowJobBeanDefinitionParser extends AbstractJobBeanDefinitionParser {
    
    @Override
    protected BeanDefinition getJobTypeConfigurationBeanDefinition(final ParserContext parserContext, final BeanDefinition jobCoreConfigurationBeanDefinition, final Element element) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(InstanceProvidedDataflowJobConfiguration.class);
        result.addConstructorArgValue(jobCoreConfigurationBeanDefinition);
        if (Strings.isNullOrEmpty(element.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE))) {
            result.addConstructorArgValue(parserContext.getRegistry().getBeanDefinition(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_REF_ATTRIBUTE)).getBeanClassName());
        } else {
            result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE));
        }
        result.addConstructorArgValue(element.getAttribute(DataflowJobBeanDefinitionParserTag.STREAMING_PROCESS_ATTRIBUTE));
        if (Strings.isNullOrEmpty(element.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE))) {
            result.addConstructorArgReference(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_REF_ATTRIBUTE));
        } else {
            result.addConstructorArgValue(null);
        }
        return result.getBeanDefinition();
    }
}
