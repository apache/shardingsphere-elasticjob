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

package com.dangdang.ddframe.job.cloud.state.ready;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 待运行作业队列服务.
 *
 * @author zhangliang
 */
public class ReadyService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    public ReadyService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        runningService = new RunningService(registryCenter);
    }
    
    /**
     * 将作业放入待执行队列.
     * 
     * @param jobName 作业名称
     * @param misfired 是否为misfire的作业
     */
    // TODO misfire作业以后考虑独立出去
    public void enqueue(final String jobName, final boolean misfired) {
        registryCenter.persistSequential(ReadyNode.getReadyJobNodePath(jobName), Boolean.toString(misfired));
    }
    
    /**
     * 从待执行队列中出队顶端作业.
     * 
     * @return 出队的作业, 队列为空则不返回数据
     */
    public Optional<JobContext> dequeue() {
        if (!registryCenter.isExisted(ReadyNode.ROOT)) {
            return Optional.absent();
        }
        List<String> jobNamesWithSequential = registryCenter.getChildrenKeys(ReadyNode.ROOT);
        for (String each : jobNamesWithSequential) {
            registryCenter.remove(ReadyNode.getReadyJobNodePath(each));
            ReadyJob readyJob = new ReadyJob(each);
            String jobName = readyJob.getJobName();
            Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
            if (!jobConfig.isPresent() || runningService.isJobRunning(jobName)) {
                if (isNeedMisfire(jobConfig)) {
                    enqueue(jobName, true);
                }
                continue;
            }
            return getJobContext(jobConfig.get());
        }
        return Optional.absent();
    }
    
    private boolean isNeedMisfire(final Optional<CloudJobConfiguration> jobConfig) {
        return jobConfig.isPresent() && jobConfig.get().isMisfire() && !Boolean.valueOf(registryCenter.get(ReadyNode.getReadyJobNodePath(jobConfig.get().getJobName())));
    }
    
    private Optional<JobContext> getJobContext(final CloudJobConfiguration jobConfig) {
        int shardingTotalCount = jobConfig.getShardingTotalCount();
        Collection<Integer> shardingItems = new ArrayList<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            shardingItems.add(i);
        }
        return Optional.of(new JobContext(jobConfig, shardingItems));
    }
    
    static final class ReadyJob {
        
        @Getter(AccessLevel.PACKAGE)
        private final String jobName;
        
        ReadyJob(final String jobNameWithSequential) {
            jobName = jobNameWithSequential.substring(0, jobNameWithSequential.length() - 10);
        }
    }
}
