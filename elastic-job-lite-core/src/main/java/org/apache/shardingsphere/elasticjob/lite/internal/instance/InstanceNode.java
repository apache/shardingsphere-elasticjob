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

import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;

/**
 * Instance node.
 */
public final class InstanceNode {
    
    public static final String ROOT = "instances";
    
    private static final String INSTANCES = ROOT + "/%s";
    
    private final String jobName;
    
    private final JobNodePath jobNodePath;
    
    public InstanceNode(final String jobName) {
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * Get job instance full path.
     *
     * @return job instance full path
     */
    public String getInstanceFullPath() {
        return jobNodePath.getFullPath(InstanceNode.ROOT);
    }
    
    /**
     * Judge path is job instance path or not.
     *
     * @param path path to be judged
     * @return path is job instance path or not
     */
    public boolean isInstancePath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(InstanceNode.ROOT));
    }
    
    boolean isLocalInstancePath(final String path) {
        return path.equals(jobNodePath.getFullPath(String.format(INSTANCES, JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId())));
    }
    
    String getLocalInstanceNode() {
        return String.format(INSTANCES, JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }
}
