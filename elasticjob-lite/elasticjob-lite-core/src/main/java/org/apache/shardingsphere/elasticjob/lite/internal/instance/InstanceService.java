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

package org.apache.shardingsphere.elasticjob.lite.internal.instance;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerNode;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.LinkedList;
import java.util.List;

/**
 * Job instance service.
 */
public final class InstanceService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final InstanceNode instanceNode;
    
    private final TriggerNode triggerNode;
    
    private final ServerService serverService;
    
    public InstanceService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        instanceNode = new InstanceNode(jobName);
        triggerNode = new TriggerNode(jobName);
        serverService = new ServerService(regCenter, jobName);
    }
    
    /**
     * Persist job online status.
     */
    public void persistOnline() {
        jobNodeStorage.fillEphemeralJobNode(instanceNode.getLocalInstancePath(), instanceNode.getLocalInstanceValue());
    }
    
    /**
     * Persist job instance.
     */
    public void removeInstance() {
        jobNodeStorage.removeJobNodeIfExisted(instanceNode.getLocalInstancePath());
    }
    
    /**
     * Get available job instances.
     *
     * @return available job instances
     */
    public List<JobInstance> getAvailableJobInstances() {
        List<JobInstance> result = new LinkedList<>();
        for (String each : jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)) {
            JobInstance jobInstance = YamlEngine.unmarshal(jobNodeStorage.getJobNodeData(instanceNode.getInstancePath(each)), JobInstance.class);
            if (null != jobInstance && serverService.isEnableServer(jobInstance.getServerIp())) {
                result.add(new JobInstance(each));
            }
        }
        return result;
    }
    
    boolean isLocalJobInstanceExisted() {
        return jobNodeStorage.isJobNodeExisted(instanceNode.getLocalInstancePath());
    }

    /**
     * Trigger all instances.
     */
    public void triggerAllInstances() {
        jobNodeStorage.removeJobNodeIfExisted(triggerNode.getTriggerRoot());
        jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT).forEach(each -> jobNodeStorage.createJobNodeIfNeeded(triggerNode.getTriggerPath(each)));
    }
}
