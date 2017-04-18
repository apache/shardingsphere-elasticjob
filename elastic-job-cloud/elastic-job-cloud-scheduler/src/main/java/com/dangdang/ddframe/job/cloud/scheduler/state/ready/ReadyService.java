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

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待运行作业队列服务.
 *
 * @author zhangliang
 * @author liguangyun
 */
@Slf4j
public final class ReadyService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final CloudJobConfigurationService configService;
    
    private final RunningService runningService;
    
    public ReadyService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configService = new CloudJobConfigurationService(regCenter);
        runningService = new RunningService(regCenter);
    }
    
    /**
     * 将瞬时作业放入待执行队列.
     * 
     * @param jobName 作业名称
     */
    public void addTransient(final String jobName) {
        if (regCenter.getNumChildren(ReadyNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add transient job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || CloudJobExecutionType.TRANSIENT != cloudJobConfig.get().getJobExecutionType()) {
            return;
        }
        String readyJobNode = ReadyNode.getReadyJobNodePath(jobName);
        String times = regCenter.getDirectly(readyJobNode);
        if (cloudJobConfig.get().getTypeConfig().getCoreConfig().isMisfire()) {
            regCenter.persist(readyJobNode, Integer.toString(null == times ? 1 : Integer.parseInt(times) + 1));
        } else {
            regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
        }
    }
    
    /**
     * 将常驻作业放入待执行队列.
     *
     * @param jobName 作业名称
     */
    public void addDaemon(final String jobName) {
        if (regCenter.getNumChildren(ReadyNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add daemon job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || CloudJobExecutionType.DAEMON != cloudJobConfig.get().getJobExecutionType() || runningService.isJobRunning(jobName)) {
            return;
        }
        regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
    }
    
    /**
     * 设置禁用错过重执行.
     * 
     * @param jobName 作业名称
     */
    public void setMisfireDisabled(final String jobName) {
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName);
        if (cloudJobConfig.isPresent() && null != regCenter.getDirectly(ReadyNode.getReadyJobNodePath(jobName))) {
            regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
        }
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
        List<String> jobNames = regCenter.getChildrenKeys(ReadyNode.ROOT);
        List<JobContext> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            if (ineligibleJobNames.contains(each)) {
                continue;
            }
            Optional<CloudJobConfiguration> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                regCenter.remove(ReadyNode.getReadyJobNodePath(each));
                continue;
            }
            if (!runningService.isJobRunning(each)) {
                result.add(JobContext.from(jobConfig.get(), ExecutionType.READY));
            }
        }
        return result;
    }
    
    /**
     * 从待执行队列中删除相关作业.
     *
     * @param jobNames 待删除的作业名集合
     */
    public void remove(final Collection<String> jobNames) {
        for (String each : jobNames) {
            String readyJobNode = ReadyNode.getReadyJobNodePath(each);
            String timesStr = regCenter.getDirectly(readyJobNode);
            int times = null == timesStr ? 0 : Integer.parseInt(timesStr);
            if (times <= 1) {
                regCenter.remove(readyJobNode);
            } else {
                regCenter.persist(readyJobNode, Integer.toString(times - 1));
            }
        }
    }
    
    /**
     * 获取待运行的全部任务.
     * 
     * @return 待运行的全部任务
     */
    public Map<String, Integer> getAllReadyTasks() {
        if (!regCenter.isExisted(ReadyNode.ROOT)) {
            return Collections.emptyMap();
        }
        List<String> jobNames = regCenter.getChildrenKeys(ReadyNode.ROOT);
        Map<String, Integer> result = new HashMap<>(jobNames.size(), 1);
        for (String each : jobNames) {
            String times = regCenter.get(ReadyNode.getReadyJobNodePath(each));
            if (!Strings.isNullOrEmpty(times)) {
                result.put(each, Integer.parseInt(times));
            }
        }
        return result;
    }
}
