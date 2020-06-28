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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;

import java.io.Serializable;

/**
 * Job settings.
 */
@Getter
@Setter
public final class JobSettings implements Serializable {
    
    private static final long serialVersionUID = -6532210090618686688L;
    
    private String jobName;
    
    private String jobType;
    
    private String cron;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds;
    
    private int reconcileIntervalMinutes;
    
    private int monitorPort = -1;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorServiceHandlerType;
    
    private String jobErrorHandlerType;
    
    private String description;
    
    private boolean streamingProcess;
    
    private String scriptCommandLine;
    
    /**
     * To YAML job configuration.
     * 
     * @return YAML job configuration
     */
    public YamlJobConfiguration toYamlJobConfiguration() {
        YamlJobConfiguration result = new YamlJobConfiguration();
        result.setJobName(jobName);
        result.setJobType(JobType.valueOf(jobType));
        result.setCron(cron);
        result.setShardingTotalCount(shardingTotalCount);
        result.setShardingItemParameters(shardingItemParameters);
        result.setJobParameter(jobParameter);
        result.setMonitorExecution(monitorExecution);
        result.setFailover(failover);
        result.setMisfire(misfire);
        result.setMaxTimeDiffSeconds(maxTimeDiffSeconds);
        result.setReconcileIntervalMinutes(reconcileIntervalMinutes);
        result.setMonitorPort(monitorPort);
        result.setJobShardingStrategyType(jobShardingStrategyType);
        result.setJobExecutorServiceHandlerType(jobExecutorServiceHandlerType);
        result.setJobErrorHandlerType(jobErrorHandlerType);
        result.setDescription(description);
        result.getProps().setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.valueOf(streamingProcess).toString());
        if (null != scriptCommandLine) {
            result.getProps().setProperty(ScriptJobExecutor.SCRIPT_KEY, scriptCommandLine);
        }
        return result;
    }
}
