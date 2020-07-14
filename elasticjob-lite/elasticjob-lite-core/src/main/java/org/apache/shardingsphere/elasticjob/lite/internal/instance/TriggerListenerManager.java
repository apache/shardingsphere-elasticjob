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

import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Job trigger listener manager.
 */
public final class TriggerListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final InstanceNode instanceNode;
    
    private final InstanceService instanceService;
    
    public TriggerListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        instanceNode = new InstanceNode(jobName);
        instanceService = new InstanceService(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new JobTriggerStatusJobListener());
    }
    
    class JobTriggerStatusJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (!InstanceOperation.TRIGGER.name().equals(data) || !instanceNode.isLocalInstancePath(path) || Type.NODE_CHANGED != eventType) {
                return;
            }
            instanceService.clearTriggerFlag();
            if (!JobRegistry.getInstance().isShutdown(jobName) && !JobRegistry.getInstance().isJobRunning(jobName)) {
                // TODO At present, it cannot be triggered when the job is running, and it will be changed to a stacked trigger in the future.
                JobRegistry.getInstance().getJobScheduleController(jobName).triggerJob();
            }
        }
    }
}
