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

package org.apache.shardingsphere.elasticjob.infra.validate;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * Properties preconditions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesPreconditions {
    
    /**
     * Check property value is required.
     * 
     * @param props properties to be checked
     * @param key property key to be checked
     */
    public static void checkRequired(final Properties props, final String key) {
        Preconditions.checkState(props.containsKey(key), "The property `%s` is required.", key);
    }
    
    /**
     * Check property value is positive integer.
     * 
     * @param props properties to be checked
     * @param key property key to be checked
     */
    public static void checkPositiveInteger(final Properties props, final String key) {
        String propertyValue = props.getProperty(key);
        if (null == propertyValue) {
            return;
        }
        int integerValue;
        try {
            integerValue = Integer.parseInt(propertyValue);
        } catch (final NumberFormatException ignored) {
            throw new IllegalArgumentException(String.format("The property `%s` should be integer.", key));
        }
        Preconditions.checkArgument(integerValue > 0, "The property `%s` should be positive.", key);
    }
}
