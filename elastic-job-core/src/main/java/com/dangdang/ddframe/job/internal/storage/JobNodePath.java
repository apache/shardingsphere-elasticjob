/**
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

package com.dangdang.ddframe.job.internal.storage;

/**
 * 作业节点路径类.
 * 
 * <p>
 * 作业节点是在普通的节点前加上作业名称的前缀.
 * </p>
 * 
 * @author zhangliang
 */
public final class JobNodePath {
    
    private final String jobName;
    
    public JobNodePath(final String jobName) {
        this.jobName = jobName;
    }
    
    /**
     * 获取节点全路径.
     * 
     * @param node 节点名称
     * @return 节点全路径
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", jobName, node);
    }
}
