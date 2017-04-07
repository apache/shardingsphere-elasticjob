/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作业注册表.
 * 
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobRegistry {
    
    private static volatile JobRegistry instance;
    
    private Map<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();
    
    private Map<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();
    
    private Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
    
    private Map<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();
    
    private Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();
    
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
     * 添加作业调度控制器.
     * 
     * @param jobName 作业名称
     * @param jobScheduleController 作业调度控制器
     * @param regCenter 注册中心
     */
    public void registerJob(final String jobName, final JobScheduleController jobScheduleController, final CoordinatorRegistryCenter regCenter) {
        schedulerMap.put(jobName, jobScheduleController);
        regCenterMap.put(jobName, regCenter);
        regCenter.addCacheData("/" + jobName);
    }
    
    /**
     * 获取作业调度控制器.
     * 
     * @param jobName 作业名称
     * @return 作业调度控制器
     */
    public JobScheduleController getJobScheduleController(final String jobName) {
        return schedulerMap.get(jobName);
    }
    
    /**
     * 获取作业注册中心.
     *
     * @param jobName 作业名称
     * @return 作业注册中心
     */
    public CoordinatorRegistryCenter getRegCenter(final String jobName) {
        return regCenterMap.get(jobName);
    }
    
    /**
     * 添加作业实例.
     *
     * @param jobName 作业名称
     * @param jobInstance 作业实例
     */
    public void addJobInstance(final String jobName, final JobInstance jobInstance) {
        jobInstanceMap.put(jobName, jobInstance);
    }
    
    /**
     * 获取作业运行实例.
     *
     * @param jobName 作业名称
     * @return 作业运行实例
     */
    public JobInstance getJobInstance(final String jobName) {
        return jobInstanceMap.get(jobName);
    }
    
    /**
     * 获取作业是否在运行.
     * 
     * @param jobName 作业名称
     * @return 作业是否在运行
     */
    public boolean isJobRunning(final String jobName) {
        Boolean result = jobRunningMap.get(jobName);
        return null == result ? false : result;
    }
    
    /**
     * 设置作业是否在运行.
     * 
     * @param jobName 作业名称
     * @param isRunning 作业是否在运行
     */
    public void setJobRunning(final String jobName, final boolean isRunning) {
        jobRunningMap.put(jobName, isRunning);
    }
    
    /**
     * 获取当前分片总数.
     *
     * @param jobName 作业名称
     * @return 当前分片总数
     */
    public int getCurrentShardingTotalCount(final String jobName) {
        Integer result = currentShardingTotalCountMap.get(jobName);
        return null == result ? 0 : result;
    }
    
    /**
     * 设置当前分片总数.
     *
     * @param jobName 作业名称
     * @param currentShardingTotalCount 当前分片总数
     */
    public void setCurrentShardingTotalCount(final String jobName, final int currentShardingTotalCount) {
        currentShardingTotalCountMap.put(jobName, currentShardingTotalCount);
    }
    
    /**
     * 终止任务调度.
     * 
     * @param jobName 作业名称
     */
    public void shutdown(final String jobName) {
        JobScheduleController scheduleController = schedulerMap.remove(jobName);
        if (null != scheduleController) {
            scheduleController.shutdown();
        }
        CoordinatorRegistryCenter regCenter = regCenterMap.remove(jobName);
        if (null != regCenter) {
            regCenter.evictCacheData("/" + jobName);
        }
        jobInstanceMap.remove(jobName);
        jobRunningMap.remove(jobName);
        currentShardingTotalCountMap.remove(jobName);
    }
    
    /**
     * 判断任务调度是否已终止.
     * 
     * @param jobName 作业名称
     * @return 任务调度是否已终止
     */
    public boolean isShutdown(final String jobName) {
        return !schedulerMap.containsKey(jobName) || !jobInstanceMap.containsKey(jobName);
    }
}
