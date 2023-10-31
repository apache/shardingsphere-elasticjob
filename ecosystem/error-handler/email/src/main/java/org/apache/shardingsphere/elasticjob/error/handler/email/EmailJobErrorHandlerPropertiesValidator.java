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

package org.apache.shardingsphere.elasticjob.error.handler.email;

import org.apache.shardingsphere.elasticjob.kernel.infra.exception.PropertiesPreconditions;
import org.apache.shardingsphere.elasticjob.kernel.executor.error.handler.JobErrorHandlerPropertiesValidator;

import java.util.Properties;

/**
 * Job error handler properties validator for email.
 */
public final class EmailJobErrorHandlerPropertiesValidator implements JobErrorHandlerPropertiesValidator {
    
    @Override
    public void validate(final Properties props) {
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.HOST);
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.PORT);
        PropertiesPreconditions.checkPositiveInteger(props, EmailPropertiesConstants.PORT);
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.USERNAME);
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.PASSWORD);
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.FROM);
        PropertiesPreconditions.checkRequired(props, EmailPropertiesConstants.TO);
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
