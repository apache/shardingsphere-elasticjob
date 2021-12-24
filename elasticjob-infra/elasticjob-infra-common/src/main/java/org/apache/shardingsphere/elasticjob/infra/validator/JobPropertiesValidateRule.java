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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * Job properties validate rule.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobPropertiesValidateRule {
    
    /**
     * Validate property value is required.
     * 
     * @param props properties to be validated
     * @param key property key to be validated
     */
    public static void validateIsRequired(final Properties props, final String key) {
        Preconditions.checkNotNull(props.getProperty(key), "The property `%s` is required.", key);
    }
    
    /**
     * Validate property value is positive integer.
     * 
     * @param props properties to be validated
     * @param key property key to be validated
     */
    public static void validateIsPositiveInteger(final Properties props, final String key) {
        String propertyValue = props.getProperty(key);
        if (null != propertyValue) {
            int integerValue;
            try {
                integerValue = Integer.parseInt(propertyValue);
            } catch (final NumberFormatException ignored) {
                throw new IllegalArgumentException(String.format("The property `%s` should be integer.", key));
            }
            Preconditions.checkArgument(integerValue > 0, "The property `%s` should be positive.", key);
        }
    }
}
