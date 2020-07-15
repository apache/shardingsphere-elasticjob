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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Job registry.
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
     * Get instance of job registry.
     * 
     * @return instance of job registry
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
     * Register registry center.
     *
     * @param jobName job name
     * @param regCenter registry center
     */
    public void registerRegistryCenter(final String jobName, final CoordinatorRegistryCenter regCenter) {
        regCenterMap.put(jobName, regCenter);
        regCenter.addCacheData("/" + jobName);
    }
    
    /**
     * Register job.
     * 
     * @param jobName job name
     * @param jobScheduleController job schedule controller
     */
    public void registerJob(final String jobName, final JobScheduleController jobScheduleController) {
        schedulerMap.put(jobName, jobScheduleController);
    }
    
    /**
     * Get job schedule controller.
     * 
     * @param jobName job name
     * @return job schedule controller
     */
    public JobScheduleController getJobScheduleController(final String jobName) {
        return schedulerMap.get(jobName);
    }
    
    /**
     * Get registry center.
     *
     * @param jobName job name
     * @return registry center
     */
    public CoordinatorRegistryCenter getRegCenter(final String jobName) {
        return regCenterMap.get(jobName);
    }
    
    /**
     * Add job instance.
     *
     * @param jobName job name
     * @param jobInstance job instance
     */
    public void addJobInstance(final String jobName, final JobInstance jobInstance) {
        jobInstanceMap.put(jobName, jobInstance);
    }
    
    /**
     * Get job instance.
     *
     * @param jobName job name
     * @return job instance
     */
    public JobInstance getJobInstance(final String jobName) {
        return jobInstanceMap.get(jobName);
    }
    
    /**
     * Judge job is running or not.
     * 
     * @param jobName job name
     * @return job is running or not
     */
    public boolean isJobRunning(final String jobName) {
        Boolean result = jobRunningMap.get(jobName);
        return null == result ? false : result;
    }
    
    /**
     * Set job running status.
     * 
     * @param jobName job name
     * @param isRunning job running status
     */
    public void setJobRunning(final String jobName, final boolean isRunning) {
        jobRunningMap.put(jobName, isRunning);
    }
    
    /**
     * Get sharding total count which running on current job server.
     *
     * @param jobName job name
     * @return sharding total count which running on current job server
     */
    public int getCurrentShardingTotalCount(final String jobName) {
        Integer result = currentShardingTotalCountMap.get(jobName);
        return null == result ? 0 : result;
    }
    
    /**
     * Set sharding total count which running on current job server.
     *
     * @param jobName job name
     * @param currentShardingTotalCount sharding total count which running on current job server
     */
    public void setCurrentShardingTotalCount(final String jobName, final int currentShardingTotalCount) {
        currentShardingTotalCountMap.put(jobName, currentShardingTotalCount);
    }
    
    /**
     * Shutdown job schedule.
     * 
     * @param jobName job name
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
     * Judge job is shutdown or not.
     * 
     * @param jobName job name
     * @return job is shutdown or not
     */
    public boolean isShutdown(final String jobName) {
        return !schedulerMap.containsKey(jobName) || !jobInstanceMap.containsKey(jobName);
    }
}
