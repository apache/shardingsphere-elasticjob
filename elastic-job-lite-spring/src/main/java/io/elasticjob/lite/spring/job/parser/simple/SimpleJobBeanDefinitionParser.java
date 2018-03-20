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

package io.elasticjob.lite.spring.job.parser.simple;

import com.google.common.base.Strings;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.spring.job.parser.common.AbstractJobBeanDefinitionParser;
import io.elasticjob.lite.spring.job.parser.common.BaseJobBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 简单作业的命名空间解析器.
 * 
 * @author caohao
 */
public final class SimpleJobBeanDefinitionParser extends AbstractJobBeanDefinitionParser {
    
    @Override
    protected BeanDefinition getJobTypeConfigurationBeanDefinition(final ParserContext parserContext, final BeanDefinition jobCoreConfigurationBeanDefinition, final Element element) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(SimpleJobConfiguration.class);
        result.addConstructorArgValue(jobCoreConfigurationBeanDefinition);
        if (Strings.isNullOrEmpty(element.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE))) {
            result.addConstructorArgValue(parserContext.getRegistry().getBeanDefinition(element.getAttribute(BaseJobBeanDefinitionParserTag.JOB_REF_ATTRIBUTE)).getBeanClassName());
        } else {
            result.addConstructorArgValue(element.getAttribute(BaseJobBeanDefinitionParserTag.CLASS_ATTRIBUTE));
        }
        return result.getBeanDefinition();
    }
}
