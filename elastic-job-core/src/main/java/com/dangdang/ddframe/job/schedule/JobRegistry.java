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

package com.dangdang.ddframe.job.schedule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 作业注册表.
 * 
 * @author zhangliang
 */
public final class JobRegistry {
    
    private static volatile JobRegistry instance;
    
    private ConcurrentMap<String, JobController> map = new ConcurrentHashMap<>();
    
    private JobRegistry() {
    }
    
    public static JobRegistry getInstance() {
        if (null == instance) {
            synchronized (JobRegistry.class) {
                if (null == instance) {
                    instance = new JobRegistry();
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加作业.
     * 
     * @param jobName 作业名称
     * @param jobController 作业控制器
     */
    public void addJob(final String jobName, final JobController jobController) {
        map.put(jobName, jobController);
    }
    
    /**
     * 获取作业.
     * 
     * @param jobName 作业名称
     */
    public JobController getJob(final String jobName) {
        return map.get(jobName);
    }
}
