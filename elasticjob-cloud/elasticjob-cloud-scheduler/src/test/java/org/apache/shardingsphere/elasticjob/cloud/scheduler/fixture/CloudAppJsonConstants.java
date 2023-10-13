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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudAppJsonConstants {
    
    private static final String APP_JSON = "{\"appName\":\"%s\",\"appURL\":\"http://localhost/app.jar\",\"bootstrapScript\":\"bin/start.sh\","
            + "\"cpuCount\":1.0,\"memoryMB\":128.0,\"appCacheEnable\":true,\"eventTraceSamplingCount\":0}";
    
    /**
     * Get app in json format.
     * @param appName app name
     * @return app in json format
     */
    public static String getAppJson(final String appName) {
        return String.format(APP_JSON, appName);
    }
}
