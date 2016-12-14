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

package com.dangdang.ddframe.job.cloud.scheduler.state.failover;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.context.TaskContext.MetaInfo;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 失效转移队列服务.
 *
 * @author zhangliang
 */
@Slf4j
public class FailoverService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    public FailoverService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configService = new ConfigurationService(regCenter);
        runningService = new RunningService();
    }
    
    /**
     * 将任务放入失效转移队列.
     *
     * @param taskContext 任务运行时上下文
     */
    public void add(final TaskContext taskContext) {
        if (regCenter.getNumChildren(FailoverNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        Optional<CloudJobConfiguration> jobConfig = configService.load(taskContext.getMetaInfo().getJobName());
        if (!jobConfig.isPresent() || JobExecutionType.DAEMON == jobConfig.get().getJobExecutionType()) {
            return;
        }
        String failoverTaskNodePath = FailoverNode.getFailoverTaskNodePath(taskContext.getMetaInfo().toString());
        if (!regCenter.isExisted(failoverTaskNodePath) && !runningService.isTaskRunning(taskContext.getMetaInfo())) {
            regCenter.persist(failoverTaskNodePath, taskContext.getId());
        }
    }
    
    /**
     * 从失效转移队列中获取所有有资格执行的作业上下文.
     *
     * @return 有资格执行的作业上下文集合
     */
    public Collection<JobContext> getAllEligibleJobContexts() {
        if (!regCenter.isExisted(FailoverNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> jobNames = regCenter.getChildrenKeys(FailoverNode.ROOT);
        Collection<JobContext> result = new ArrayList<>(jobNames.size());
        Set<HashCode> assignedTasks = new HashSet<>(jobNames.size() * 10, 1);
        for (String each : jobNames) {
            List<String> taskIdList = regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath(each));
            if (taskIdList.isEmpty()) {
                regCenter.remove(FailoverNode.getFailoverJobNodePath(each));
                continue;
            }
            Optional<CloudJobConfiguration> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                regCenter.remove(FailoverNode.getFailoverJobNodePath(each));
                continue;
            }
            List<Integer> assignedShardingItems = getAssignedShardingItems(each, taskIdList, assignedTasks);
            if (!assignedShardingItems.isEmpty()) {
                if (jobConfig.isPresent()) {
                    result.add(new JobContext(jobConfig.get(), assignedShardingItems, ExecutionType.FAILOVER));    
                }
            }
        }
        return result;
    }
    
    private List<Integer> getAssignedShardingItems(final String jobName, final List<String> taskIdList, final Set<HashCode> assignedTasks) {
        List<Integer> result = new ArrayList<>(taskIdList.size());
        for (String each : taskIdList) {
            TaskContext.MetaInfo metaInfo = TaskContext.MetaInfo.from(each);
            if (assignedTasks.add(Hashing.md5().newHasher().putString(jobName, Charsets.UTF_8).putInt(metaInfo.getShardingItems().get(0)).hash()) && !runningService.isTaskRunning(metaInfo)) {
                result.add(metaInfo.getShardingItems().get(0));
            }
        }
        return result;
    }
    
    /**
     * 从失效转移队列中删除相关任务.
     * 
     * @param metaInfoList 待删除的任务元信息集合
     */
    public void remove(final Collection<TaskContext.MetaInfo> metaInfoList) {
        for (TaskContext.MetaInfo each : metaInfoList) {
            regCenter.remove(FailoverNode.getFailoverTaskNodePath(each.toString()));
        }
    }
    
    /**
     * 从失效转移队列中查找任务.
     *
     * @param metaInfo 任务元信息
     * @return 失效转移任务Id
     */
    public Optional<String> getTaskId(final MetaInfo metaInfo) {
        String failoverTaskNodePath = FailoverNode.getFailoverTaskNodePath(metaInfo.toString());
        Optional<String> result = Optional.absent();
        if (regCenter.isExisted(failoverTaskNodePath)) {
            result = Optional.of(regCenter.get(failoverTaskNodePath));
        }
        return result;
    }
}
