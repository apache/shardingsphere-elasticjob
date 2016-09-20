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

package com.dangdang.ddframe.job.cloud.scheduler.state.ready;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.state.UniqueJob;
import com.dangdang.ddframe.job.cloud.scheduler.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 待运行作业队列服务.
 *
 * @author zhangliang
 */
public class ReadyService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    private final MisfiredService misfiredService;
    
    public ReadyService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configService = new ConfigurationService(regCenter);
        runningService = new RunningService(regCenter);
        misfiredService = new MisfiredService(regCenter);
    }
    
    /**
     * 将瞬时作业放入待执行队列.
     * 
     * @param jobName 作业名称
     */
    public void addTransient(final String jobName) {
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || JobExecutionType.TRANSIENT != cloudJobConfig.get().getJobExecutionType()) {
            return;
        }
        regCenter.persist(ReadyNode.getReadyJobNodePath(new UniqueJob(jobName).getUniqueName()), "");
    }
    
    /**
     * 将常驻作业放入待执行队列.
     *
     * @param jobName 作业名称
     */
    public void addDaemon(final String jobName) {
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || JobExecutionType.DAEMON != cloudJobConfig.get().getJobExecutionType()) {
            return;
        }
        for (String each : regCenter.getChildrenKeys(ReadyNode.ROOT)) {
            if (UniqueJob.from(each).getJobName().equals(jobName)) {
                return;
            }
        }
        regCenter.persist(ReadyNode.getReadyJobNodePath(new UniqueJob(jobName).getUniqueName()), "");
    }
    
    /**
     * 从待执行队列中获取所有有资格执行的作业上下文.
     *
     * @param ineligibleJobContexts 无资格执行的作业上下文
     * @return 有资格执行的作业上下文集合
     */
    public Collection<JobContext> getAllEligibleJobContexts(final Collection<JobContext> ineligibleJobContexts) {
        if (!regCenter.isExisted(ReadyNode.ROOT)) {
            return Collections.emptyList();
        }
        Collection<String> ineligibleJobNames = Collections2.transform(ineligibleJobContexts, new Function<JobContext, String>() {
            
            @Override
            public String apply(final JobContext input) {
                return input.getJobConfig().getJobName();
            }
        });
        List<String> uniqueNames = regCenter.getChildrenKeys(ReadyNode.ROOT);
        List<JobContext> result = new ArrayList<>(uniqueNames.size());
        Set<String> assignedJobNames = new HashSet<>(uniqueNames.size(), 1);
        for (String each : uniqueNames) {
            String jobName = UniqueJob.from(each).getJobName();
            if (assignedJobNames.contains(jobName) || ineligibleJobNames.contains(jobName)) {
                continue;
            }
            Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
            if (!jobConfig.isPresent()) {
                regCenter.remove(ReadyNode.getReadyJobNodePath(each));
                continue;
            }
            if (runningService.isJobRunning(jobName)) {
                if (jobConfig.get().getTypeConfig().getCoreConfig().isMisfire()) {
                    misfiredService.add(jobName);
                }
                if (JobExecutionType.DAEMON == jobConfig.get().getJobExecutionType()) {
                    result.add(JobContext.from(jobConfig.get(), ExecutionType.READY));
                }
                continue;
            }
            result.add(JobContext.from(jobConfig.get(), ExecutionType.READY));
            assignedJobNames.add(jobName);
        }
        return result;
    }
    
    /**
     * 从待执行队列中删除相关作业.
     *
     * @param jobNames 待删除的作业名集合
     */
    public void remove(final Collection<String> jobNames) {
        List<UniqueJob> uniqueJobs = Lists.transform(regCenter.getChildrenKeys(ReadyNode.ROOT), new Function<String, UniqueJob>() {
            
            @Override
            public UniqueJob apply(final String input) {
                return UniqueJob.from(input);
            }
        });
        for (String each : jobNames) {
            Optional<UniqueJob> uniqueJob = find(each, uniqueJobs);
            if (uniqueJob.isPresent()) {
                regCenter.remove(ReadyNode.getReadyJobNodePath(uniqueJob.get().getUniqueName()));
            }
        }
    }
    
    private Optional<UniqueJob> find(final String jobName, final List<UniqueJob> uniqueJobs) {
        for (UniqueJob each : uniqueJobs) {
            if (jobName.equals(each.getJobName())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
