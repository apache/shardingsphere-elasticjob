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

package com.dangdang.ddframe.job.cloud.scheduler.state.misfired;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 错过执行的作业队列服务.
 *
 * @author zhangliang
 */
@Slf4j
public class MisfiredService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    public MisfiredService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configService = new ConfigurationService(regCenter);
        runningService = new RunningService(regCenter);
    }
    
    /**
     * 将作业放入错过执行队列.
     * 
     * @param jobName 作业名称
     */
    public void add(final String jobName) {
        if (regCenter.getChildrenKeys(MisfiredNode.ROOT).size() > BootstrapEnvironment.JOB_STATE_QUEUE_SIZE) {
            log.error("Cannot add job, caused by read state queue size is larger than {}.", BootstrapEnvironment.JOB_STATE_QUEUE_SIZE);
            return;
        }
        Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
        if (!jobConfig.isPresent() || JobExecutionType.DAEMON == jobConfig.get().getJobExecutionType()) {
            return;
        }
        if (!regCenter.isExisted(MisfiredNode.getMisfiredJobNodePath(jobName))) {
            regCenter.persist(MisfiredNode.getMisfiredJobNodePath(jobName), "");
        }
    }
    
    /**
     * 从错过执行队列中获取所有有资格执行的作业上下文.
     *
     * @param ineligibleJobContexts 无资格执行的作业上下文
     * @return 有资格执行的作业上下文集合
     */
    public Collection<JobContext> getAllEligibleJobContexts(final Collection<JobContext> ineligibleJobContexts) {
        if (!regCenter.isExisted(MisfiredNode.ROOT)) {
            return Collections.emptyList();
        }
        Collection<String> ineligibleJobNames = Collections2.transform(ineligibleJobContexts, new Function<JobContext, String>() {
            
            @Override
            public String apply(final JobContext input) {
                return input.getJobConfig().getJobName();
            }
        });
        List<String> jobNames = regCenter.getChildrenKeys(MisfiredNode.ROOT);
        Collection<JobContext> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                regCenter.remove(MisfiredNode.getMisfiredJobNodePath(each));
                continue;
            }
            if (!ineligibleJobNames.contains(each) && !runningService.isJobRunning(each)) {
                if (jobConfig.isPresent()) {
                    result.add(JobContext.from(jobConfig.get(), ExecutionType.MISFIRED));    
                }
            }
        }
        return result;
    }
    
    /**
     * 从错过执行队列中删除相关作业.
     * 
     * @param jobNames 待删除的作业名集合
     */
    public void remove(final Collection<String> jobNames) {
        for (String each : jobNames) {
            regCenter.remove(MisfiredNode.getMisfiredJobNodePath(each));
        }
    }
}
