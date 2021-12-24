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

import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerPropertiesValidator;
import org.apache.shardingsphere.elasticjob.infra.validator.JobPropertiesValidateRule;

import java.util.Properties;

/**
 * Job error handler properties validator for dingtalk.
 */
public final class DingtalkJobErrorHandlerPropertiesValidator implements JobErrorHandlerPropertiesValidator {
    
    @Override
    public void validate(final Properties props) {
        JobPropertiesValidateRule.validateIsRequired(props, DingtalkPropertiesConstants.WEBHOOK);
        JobPropertiesValidateRule.validateIsPositiveInteger(props, DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS);
        JobPropertiesValidateRule.validateIsPositiveInteger(props, DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public String getType() {
        return "DINGTALK";
    }
}
