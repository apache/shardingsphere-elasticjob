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

package org.apache.shardingsphere.elasticjob.lite.fixture.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationUtil {
    
    /**
     * Create the configuration of simple job.
     *
     * @return job configuration
     */
    public static JobConfiguration createSimpleJobConfiguration() {
        return JobConfiguration.newBuilder("test_job", JobType.SIMPLE, "0/1 * * * * ?", 3).build();
    }
    
    /**
     * Create the configuration of simple job.
     *
     * @param overwrite whether overwrite the config
     * @return job configuration
     */
    public static JobConfiguration createSimpleJobConfiguration(final boolean overwrite) {
        return JobConfiguration.newBuilder("test_job", JobType.SIMPLE, "0/1 * * * * ?", 3).overwrite(overwrite).build();
    }
}
