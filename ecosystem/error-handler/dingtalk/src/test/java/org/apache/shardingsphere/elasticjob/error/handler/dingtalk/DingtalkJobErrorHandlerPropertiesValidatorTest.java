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

import org.apache.shardingsphere.elasticjob.spi.executor.error.handler.JobErrorHandlerPropertiesValidator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DingtalkJobErrorHandlerPropertiesValidatorTest {
    
    @Test
    void assertValidateWithNormal() {
        Properties properties = new Properties();
        properties.setProperty(DingtalkPropertiesConstants.WEBHOOK, "webhook");
        properties.setProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECONDS, "1000");
        properties.setProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, "2000");
        TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "DINGTALK").validate(properties);
    }
    
    @Test
    void assertValidateWithPropsIsNull() {
        assertThrows(NullPointerException.class, () -> TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "DINGTALK").validate(null));
    }
    
    @Test
    void assertValidateWithWebhookIsNull() {
        try {
            TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "DINGTALK").validate(new Properties());
        } catch (final IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(String.format("The property `%s` is required.", DingtalkPropertiesConstants.WEBHOOK)));
        }
    }
}
