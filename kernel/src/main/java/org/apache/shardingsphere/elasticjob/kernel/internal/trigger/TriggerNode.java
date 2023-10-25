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

package org.apache.shardingsphere.elasticjob.kernel.internal.trigger;

import org.apache.shardingsphere.elasticjob.infra.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodePath;

/**
 * Trigger node.
 */
public final class TriggerNode {
    
    public static final String ROOT = "trigger";
    
    private static final String TRIGGER = ROOT + "/%s";
    
    private final String jobName;
    
    private final JobNodePath jobNodePath;
    
    public TriggerNode(final String jobName) {
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * Is local trigger path.
     *
     * @param path path
     * @return is local trigger path or not
     */
    public boolean isLocalTriggerPath(final String path) {
        JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
        return null != jobInstance && path.equals(jobNodePath.getFullPath(String.format(TRIGGER, jobInstance.getJobInstanceId())));
    }
    
    /**
     * Get local trigger path.
     *
     * @return local trigger path
     */
    public String getLocalTriggerPath() {
        return getTriggerPath(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }
    
    /**
     * Get trigger path.
     *
     * @param instanceId instance id
     * @return trigger path
     */
    public String getTriggerPath(final String instanceId) {
        return String.format(TRIGGER, instanceId);
    }
    
    /**
     * Get trigger root.
     *
     * @return trigger root
     */
    public String getTriggerRoot() {
        return ROOT;
    }
}
