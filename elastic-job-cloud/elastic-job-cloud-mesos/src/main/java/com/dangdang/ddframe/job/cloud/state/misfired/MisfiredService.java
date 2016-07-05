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

package com.dangdang.ddframe.job.cloud.state.misfired;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.state.SequentialJob;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 错过执行的作业队列服务.
 *
 * @author zhangliang
 */
public class MisfiredService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    public MisfiredService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        runningService = new RunningService(registryCenter);
    }
    
    /**
     * 将作业放入错过执行队列.
     * 
     * @param jobName 作业名称
     */
    public void add(final String jobName) {
        if (!registryCenter.isExisted(MisfiredNode.getMisfiredJobNodePath(jobName))) {
            registryCenter.persist(MisfiredNode.getMisfiredJobNodePath(jobName), "");
        }
    }
    
    /**
     * 从错过执行队列中获取所有有资格执行的作业上下文.
     *
     * @param ineligibleJobContexts 无资格执行的作业上下文
     * @return 有资格执行的作业上下文集合
     */
    public Map<String, JobContext> getAllEligibleJobContexts(final Collection<JobContext> ineligibleJobContexts) {
        if (!registryCenter.isExisted(MisfiredNode.ROOT)) {
            return Collections.emptyMap();
        }
        Collection<String> ineligibleJobNames = Collections2.transform(ineligibleJobContexts, new Function<JobContext, String>() {
            
            @Override
            public String apply(final JobContext input) {
                return input.getJobConfig().getJobName();
            }
        });
        List<String> jobNames = registryCenter.getChildrenKeys(MisfiredNode.ROOT);
        Map<String, JobContext> result = new HashMap<>(jobNames.size(), 1);
        for (String each : jobNames) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                registryCenter.remove(MisfiredNode.getMisfiredJobNodePath(each));
                continue;
            }
            if (!ineligibleJobNames.contains(each) && !runningService.isJobRunning(each)) {
                result.put(each, JobContext.from(jobConfig.get()));
            }
        }
        return result;
    }
    
    /**
     * 从错过执行队列中删除相关作业.
     * 
     * @param jobNamesWithSequential 待删除的作业名集合
     */
    public void remove(final Collection<String> jobNamesWithSequential) {
        for (String each : jobNamesWithSequential) {
            registryCenter.remove(MisfiredNode.getMisfiredJobNodePath(each));
        }
    }
}
