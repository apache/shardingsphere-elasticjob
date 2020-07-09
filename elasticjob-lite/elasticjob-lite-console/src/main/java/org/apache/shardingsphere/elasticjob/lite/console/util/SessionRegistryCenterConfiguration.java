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

package org.apache.shardingsphere.elasticjob.lite.console.util;

import org.apache.shardingsphere.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Registry center configuration configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionRegistryCenterConfiguration {
    
    private static RegistryCenterConfiguration regCenterConfig;
    
    /**
     * Get registry center configuration.
     *
     * @return registry center configuration
     */
    public static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        return regCenterConfig;
    }
    
    /**
     * Set registry center configuration.
     * 
     * @param regCenterConfig registry center configuration
     */
    public static void setRegistryCenterConfiguration(final RegistryCenterConfiguration regCenterConfig) {
        SessionRegistryCenterConfiguration.regCenterConfig = regCenterConfig;
    }
}
