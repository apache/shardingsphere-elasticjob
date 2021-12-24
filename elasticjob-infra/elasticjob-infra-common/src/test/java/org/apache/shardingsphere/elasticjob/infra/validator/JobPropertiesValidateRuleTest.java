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

package org.apache.shardingsphere.elasticjob.infra.validator;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JobPropertiesValidateRuleTest {
    
    @Test
    public void assertValidateIsRequiredWithValidateError() {
        try {
            JobPropertiesValidateRule.validateIsRequired(new Properties(), "key");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), is(String.format("The property `%s` is required.", "key")));
        }
    }
    
    @Test
    public void assertValidateIsRequiredWithNormal() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        JobPropertiesValidateRule.validateIsRequired(properties, "key");
    }
    
    @Test
    public void assertValidateIsPositiveIntegerWithValueNoExist() {
        JobPropertiesValidateRule.validateIsPositiveInteger(new Properties(), "key");
    }
    
    @Test
    public void assertValidateIsPositiveIntegerWithNormal() {
        Properties properties = new Properties();
        properties.setProperty("key", "1");
        JobPropertiesValidateRule.validateIsPositiveInteger(new Properties(), "key");
    }
    
    @Test
    public void assertValidateIsPositiveIntegerWithWrongString() {
        Properties properties = new Properties();
        properties.setProperty("key", "wrong_value");
        try {
            JobPropertiesValidateRule.validateIsPositiveInteger(properties, "key");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(String.format("The property `%s` should be integer.", "key")));
        }
    }
    
    @Test
    public void assertValidateIsPositiveIntegerWithNegativeNumber() {
        Properties properties = new Properties();
        properties.setProperty("key", "-1");
        try {
            JobPropertiesValidateRule.validateIsPositiveInteger(properties, "key");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(String.format("The property `%s` should be positive.", "key")));
        }
    }
}
