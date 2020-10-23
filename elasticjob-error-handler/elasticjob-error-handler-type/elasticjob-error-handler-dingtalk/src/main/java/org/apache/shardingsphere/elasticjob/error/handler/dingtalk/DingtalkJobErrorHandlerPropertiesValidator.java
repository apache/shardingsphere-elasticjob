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

package org.apache.shardingsphere.elasticjob.error.handler.dingtalk;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerPropertiesValidator;

import java.util.Properties;

/**
 * Job error handler properties validator for dingtalk.
 */
public final class DingtalkJobErrorHandlerPropertiesValidator implements JobErrorHandlerPropertiesValidator {
    
    @Override
    public void validate(final Properties props) {
        validateIsRequired(DingtalkPropertiesConstants.WEBHOOK, props);
        validateIsPositiveInteger(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, props);
        validateIsPositiveInteger(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECONDS, props);
    }
    
    private void validateIsRequired(final String propertyKey, final Properties props) {
        Preconditions.checkNotNull(props.getProperty(propertyKey), "The property `%s` is required.", propertyKey);
    }
    
    private void validateIsPositiveInteger(final String propertyKey, final Properties props) {
        String propertyValue = props.getProperty(propertyKey);
        if (null != propertyValue) {
            int integerValue;
            try {
                integerValue = Integer.parseInt(propertyValue);
            } catch (final NumberFormatException ignored) {
                throw new IllegalArgumentException(String.format("The property `%s` should be integer.", propertyKey));
            }
            Preconditions.checkArgument(integerValue > 0, "The property `%s` should be positive.", propertyKey);
        }
    }
    
    @Override
    public String getType() {
        return "DINGTALK";
    }
}
