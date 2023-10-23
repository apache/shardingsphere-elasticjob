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

import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerPropertiesValidator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailJobErrorHandlerPropertiesValidatorTest {
    
    @Test
    void assertValidateWithNormal() {
        Properties properties = new Properties();
        properties.setProperty(EmailPropertiesConstants.HOST, "host");
        properties.setProperty(EmailPropertiesConstants.SSL_TRUST, "*");
        properties.setProperty(EmailPropertiesConstants.PORT, "465");
        properties.setProperty(EmailPropertiesConstants.USERNAME, "username");
        properties.setProperty(EmailPropertiesConstants.PASSWORD, "password");
        properties.setProperty(EmailPropertiesConstants.FROM, "from@xxx.xx");
        properties.setProperty(EmailPropertiesConstants.TO, "to@xxx.xx");
        TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "EMAIL").validate(properties);
    }
    
    @Test
    void assertValidateWithPropsIsNull() {
        assertThrows(NullPointerException.class, () -> TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "EMAIL").validate(null));
    }
    
    @Test
    void assertValidateWithHostIsNull() {
        try {
            TypedSPILoader.getService(JobErrorHandlerPropertiesValidator.class, "EMAIL").validate(new Properties());
        } catch (final NullPointerException ex) {
            assertThat(ex.getMessage(), is(String.format("The property `%s` is required.", EmailPropertiesConstants.HOST)));
        }
    }
}
