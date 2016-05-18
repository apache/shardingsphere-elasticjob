/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.console.service;

public interface JobOperationService {
    
    void pauseJob(String jobName, String serverIp);
    
    void resumeJob(String jobName, String serverIp);
    
    void pauseAllJobsByJobName(String jobName);
    
    void resumeAllJobsByJobName(String jobName);
    
    void pauseAllJobsByServer(String serverIp);
    
    void resumeAllJobsByServer(String serverIp);
    
    void shutdownJob(String jobName, String serverIp);
    
    boolean removeJob(String jobName, String serverIp);

    void disableJob(String jobName, String serverIp);

    void enableJob(String jobName, String serverIp);
}
