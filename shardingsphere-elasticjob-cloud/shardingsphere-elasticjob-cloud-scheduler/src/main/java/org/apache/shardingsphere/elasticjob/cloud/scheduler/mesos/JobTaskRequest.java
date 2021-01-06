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

import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import com.netflix.fenzo.ConstraintEvaluator;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.VMTaskFitnessCalculator;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Job task request.
 */
@RequiredArgsConstructor
public final class JobTaskRequest implements TaskRequest {
    
    private final TaskContext taskContext;
    
    private final CloudJobConfiguration cloudJobConfig;
    
    @Override
    public String getId() {
        return taskContext.getId();
    }
    
    @Override
    public String taskGroupName() {
        return "";
    }
    
    @Override
    public double getCPUs() {
        return cloudJobConfig.getCpuCount();
    }
    
    @Override
    public double getMemory() {
        return cloudJobConfig.getMemoryMB();
    }
    
    @Override
    public double getNetworkMbps() {
        return 0;
    }
    
    @Override
    public double getDisk() {
        return 10d;
    }
    
    @Override
    public int getPorts() {
        return 1;
    }
    
    @Override
    public Map<String, Double> getScalarRequests() {
        return null;
    }
    
    @Override
    public List<? extends ConstraintEvaluator> getHardConstraints() {
        return Collections.singletonList(AppConstraintEvaluator.getInstance());
    }
    
    @Override
    public List<? extends VMTaskFitnessCalculator> getSoftConstraints() {
        return null;
    }
    
    @Override
    public void setAssignedResources(final AssignedResources assignedResources) {
    }
    
    @Override
    public AssignedResources getAssignedResources() {
        return null;
    }
    
    @Override
    public Map<String, NamedResourceSetRequest> getCustomNamedResources() {
        return Collections.emptyMap();
    }
}
