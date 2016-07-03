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

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.state.running.RunningTaskService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.List;

/**
 * 待运行作业队列服务.
 *
 * @author zhangliang
 */
public class ReadyJobQueueService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final RunningTaskService runningTaskService;
    
    public ReadyJobQueueService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        runningTaskService = new RunningTaskService(registryCenter);
    }
    
    /**
     * 将作业放入待执行队列.
     * 
     * @param jobName 作业名称
     */
    public void enqueue(final String jobName) {
        registryCenter.persistSequential(ReadyJobQueueNode.getReadyJobNodePath(jobName));
    }
    
    /**
     * 从待执行队列中获取顶端作业名称.
     * 
     * @return 出队的作业名称, 队列为空则不返回数据
     */
    public Optional<String> dequeue() {
        if (!registryCenter.isExisted(ReadyJobQueueNode.ROOT)) {
            return Optional.absent();
        }
        List<String> jobNamesWithSequential = registryCenter.getChildrenKeys(ReadyJobQueueNode.ROOT);
        for (String each : jobNamesWithSequential) {
            ReadyJob readyJob = new ReadyJob(each);
            Optional<CloudJobConfiguration> jobConfig = configService.load(readyJob.getJobName());
            if (!jobConfig.isPresent()) {
                registryCenter.remove(ReadyJobQueueNode.getReadyJobNodePath(each));
                break;
            }
            if (!runningTaskService.isJobRunning(readyJob.getJobName())) {
                registryCenter.remove(ReadyJobQueueNode.getReadyJobNodePath(each));
                return Optional.of(readyJob.getJobName());
            }
            if (!jobConfig.get().isMisfire()) {
                registryCenter.remove(ReadyJobQueueNode.getReadyJobNodePath(each));
            }
        }
        return Optional.absent();
    }
}
