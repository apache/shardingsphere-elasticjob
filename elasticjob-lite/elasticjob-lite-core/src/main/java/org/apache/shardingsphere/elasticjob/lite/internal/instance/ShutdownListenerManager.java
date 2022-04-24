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

import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

/**
 * Job instance shutdown listener manager.
 */
public final class ShutdownListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final InstanceNode instanceNode;
    
    private final InstanceService instanceService;
    
    private final SchedulerFacade schedulerFacade;
    
    public ShutdownListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        instanceNode = new InstanceNode(jobName);
        instanceService = new InstanceService(regCenter, jobName);
        schedulerFacade = new SchedulerFacade(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new InstanceShutdownStatusJobListener());
    }
    
    class InstanceShutdownStatusJobListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (!JobRegistry.getInstance().isShutdown(jobName) && !JobRegistry.getInstance().getJobScheduleController(jobName).isPaused()
                    && isRemoveInstance(event.getKey(), event.getType()) && !isReconnectedRegistryCenter()) {
                schedulerFacade.shutdownInstance();
            }
        }
        
        private boolean isRemoveInstance(final String path, final Type eventType) {
            return instanceNode.isLocalInstancePath(path) && Type.DELETED == eventType;
        }
        
        private boolean isReconnectedRegistryCenter() {
            return instanceService.isLocalJobInstanceExisted();
        }
    }
}
