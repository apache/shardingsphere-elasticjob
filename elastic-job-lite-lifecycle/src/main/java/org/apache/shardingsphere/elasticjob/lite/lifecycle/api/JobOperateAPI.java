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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.api;

/**
 * Job operate API.
 */
public interface JobOperateAPI {
    
    /**
     * Trigger job to run at once.
     *
     * <p>Job will not start until it does not conflict with the last running job, and this tag will be automatically cleaned up after it starts.</p>
     *
     * @param jobName job name
     */
    void trigger(String jobName);
    
    /**
     * Disable job.
     * 
     * <p>Will cause resharding.</p>
     *
     * @param jobName job name
     * @param serverIp server IP address
     */
    void disable(String jobName, String serverIp);
    
    /**
     * Enable job.
     * 
     * @param jobName job name
     * @param serverIp server IP address
     */
    void enable(String jobName, String serverIp);
    
    /**
     * Shutdown Job.
     *
     * @param jobName job name
     * @param serverIp server IP address
     */
    void shutdown(String jobName, String serverIp);
    
    /**
     * Remove job.
     * 
     * @param jobName job name
     * @param serverIp server IP address
     */
    void remove(String jobName, String serverIp);
}
