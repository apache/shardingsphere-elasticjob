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

package com.dangdang.ddframe.job.console.util;

public final class JobNodePath {
    
    private JobNodePath() {
    }
    
    public static String getConfigNodePath(final String jobName, final String nodeName) {
        return String.format("/%s/config/%s", jobName, nodeName);
    }
    
    public static String getServerNodePath(final String jobName) {
        return String.format("/%s/servers", jobName);
    }
    
    public static String getServerNodePath(final String jobName, final String serverIp) {
        return String.format("%s/%s", getServerNodePath(jobName), serverIp);
    }
    
    public static String getServerNodePath(final String jobName, final String serverIp, final String nodeName) {
        return String.format("%s/%s/%s", getServerNodePath(jobName), serverIp, nodeName);
    }
    
    public static String getExecutionNodePath(final String jobName) {
        return String.format("/%s/execution", jobName);
    }
    
    public static String getExecutionNodePath(final String jobName, final String item, final String nodeName) {
        return String.format("%s/%s/%s", getExecutionNodePath(jobName), item, nodeName);
    }
    
    public static String getLeaderNodePath(final String jobName, final String nodeName) {
        return String.format("/%s/leader/%s", jobName, nodeName);
    }
}
