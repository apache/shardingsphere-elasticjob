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

package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * Guarantee listener manager.
 */
public final class GuaranteeListenerManager extends AbstractListenerManager {
    
    private final GuaranteeNode guaranteeNode;
    
    private final Collection<ElasticJobListener> elasticJobListeners;
    
    public GuaranteeListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners) {
        super(regCenter, jobName);
        this.guaranteeNode = new GuaranteeNode(jobName);
        this.elasticJobListeners = elasticJobListeners;
    }
    
    @Override
    public void start() {
        addDataListener(new StartedNodeRemovedJobListener());
        addDataListener(new CompletedNodeRemovedJobListener());
    }
    
    class StartedNodeRemovedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (Type.NODE_DELETED == eventType && guaranteeNode.isStartedRootNode(path)) {
                for (ElasticJobListener each : elasticJobListeners) {
                    if (each instanceof AbstractDistributeOnceElasticJobListener) {
                        ((AbstractDistributeOnceElasticJobListener) each).notifyWaitingTaskStart();
                    }
                }
            }
        }
    }
    
    class CompletedNodeRemovedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (Type.NODE_DELETED == eventType && guaranteeNode.isCompletedRootNode(path)) {
                for (ElasticJobListener each : elasticJobListeners) {
                    if (each instanceof AbstractDistributeOnceElasticJobListener) {
                        ((AbstractDistributeOnceElasticJobListener) each).notifyWaitingTaskComplete();
                    }
                }
            }
        }
    }
}
