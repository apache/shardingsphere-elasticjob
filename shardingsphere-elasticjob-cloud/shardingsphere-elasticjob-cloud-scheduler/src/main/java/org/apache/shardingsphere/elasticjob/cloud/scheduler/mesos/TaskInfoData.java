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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task info data.
 */
@RequiredArgsConstructor
public final class TaskInfoData {
    
    private final ShardingContexts shardingContexts;
    
    private final CloudJobConfiguration cloudJobConfig;
    
    /**
     * Serialize.
     * 
     * @return byte array
     */
    public byte[] serialize() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(cloudJobConfig.getJobConfig())));
        return SerializationUtils.serialize((LinkedHashMap) result);
    }
}
