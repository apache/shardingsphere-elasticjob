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

package org.apache.shardingsphere.elasticjob.lite.console.service;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.ServerStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.ShardingOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.ShardingStatisticsAPI;

public interface JobAPIService {
    
    /**
     * Job configuration API.
     *
     * @return job configuration API
     */
    JobConfigurationAPI getJobConfigurationAPI();
    
    /**
     * Job operate API.
     *
     * @return Job operate API
     */
    JobOperateAPI getJobOperatorAPI();
    
    /**
     * Sharding operate API.
     *
     * @return sharding operate API
     */
    ShardingOperateAPI getShardingOperateAPI();
    
    /**
     * Job statistics API.
     *
     * @return job statistics API
     */
    JobStatisticsAPI getJobStatisticsAPI();
    
    /**
     * Servers statistics API.
     *
     * @return server statistics API
     */
    ServerStatisticsAPI getServerStatisticsAPI();
    
    /**
     * Sharding statistics API.
     *
     * @return sharding statistics API
     */
    ShardingStatisticsAPI getShardingStatisticsAPI();
}
