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

package com.dangdang.ddframe.job.internal.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobScheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业注册表.
 * 
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobRegistry {
    
    private static volatile JobRegistry instance;
    
    private Map<String, JobScheduler> schedulerMap = new ConcurrentHashMap<>();
    
    private ConcurrentHashMap<String, ElasticJob> instanceMap = new ConcurrentHashMap<>();
    
    /**
     * 获取作业注册表实例.
     * 
     * @return 作业注册表实例
     */
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
     * 添加作业控制器.
     * 
     * @param jobName 作业名称
     * @param jobScheduler 作业控制器
     */
    public void addJobScheduler(final String jobName, final JobScheduler jobScheduler) {
        schedulerMap.put(jobName, jobScheduler);
    }
    
    /**
     * 获取作业控制器.
     * 
     * @param jobName 作业名称
     * @return 作业控制器
     */
    public JobScheduler getJobScheduler(final String jobName) {
        return schedulerMap.get(jobName);
    }
    
    /**
     * 添加作业实例.
     * 
     * @param jobName 作业名称
     * @param job 作业实例
     */
    public void addJobInstance(final String jobName, final ElasticJob job) {
        instanceMap.putIfAbsent(jobName, job);
    }
    
    /**
     * 获取作业实例.
     * 
     * @param jobName 作业名称
     * @return 作业实例
     */
    public ElasticJob getJobInstance(final String jobName) {
        return instanceMap.get(jobName);
    }
}
