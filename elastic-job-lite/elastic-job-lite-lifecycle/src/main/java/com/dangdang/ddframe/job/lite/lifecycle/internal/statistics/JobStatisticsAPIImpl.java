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

package com.dangdang.ddframe.job.lite.lifecycle.internal.statistics;

import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ExecutionInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 作业状态展示的实现类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JobStatisticsAPIImpl implements JobStatisticsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public JobBriefInfo getJobBriefInfo(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        String liteJobConfigJson = regCenter.get(jobNodePath.getConfigNodePath());
        if (null == liteJobConfigJson) {
            return null;
        }
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.fromJson(liteJobConfigJson);
        result.setJobType(liteJobConfig.getTypeConfig().getJobType().name());
        result.setDescription(liteJobConfig.getTypeConfig().getCoreConfig().getDescription());
        result.setStatus(getJobStatus(jobName));
        result.setCron(liteJobConfig.getTypeConfig().getCoreConfig().getCron());
        return result;
    }
    
    @Override
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        List<String> jobNames = regCenter.getChildrenKeys("/");
        List<JobBriefInfo> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            JobBriefInfo jobBriefInfo = getJobBriefInfo(each);
            if (null != jobBriefInfo) {
                result.add(jobBriefInfo);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    private JobBriefInfo.JobStatus getJobStatus(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
        int okCount = 0;
        int crashedCount = 0;
        int disabledCount = 0;
        for (String each : servers) {
            switch (getServerStatus(jobName, each)) {
                case READY:
                case RUNNING:
                    okCount++;
                    break;
                case DISABLED:
                case PAUSED:
                    disabledCount++;
                    break;
                case CRASHED:
                case SHUTDOWN:
                    crashedCount++;
                    break;
                default:
                    break;
            }
        }
        return JobBriefInfo.JobStatus.getJobStatus(okCount, crashedCount, disabledCount, servers.size());
    }
    
    @Override
    public Collection<ServerInfo> getServers(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        List<String> serverIps = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
        Collection<ServerInfo> result = new ArrayList<>(serverIps.size());
        for (String each : serverIps) {
            result.add(getJobServer(jobName, each));
        }
        return result;
    }
    
    private ServerInfo getJobServer(final String jobName, final String serverIp) {
        ServerInfo result = new ServerInfo();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        result.setJobName(jobName);
        result.setIp(serverIp);
        result.setHostName(regCenter.get(jobNodePath.getServerNodePath(serverIp, "hostName")));
        result.setSharding(regCenter.get(jobNodePath.getServerNodePath(serverIp, "sharding")));
        result.setStatus(getServerStatus(jobName, serverIp));
        return result;
    }
    
    private ServerInfo.ServerStatus getServerStatus(final String jobName, final String serverIp) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String status = regCenter.get(jobNodePath.getServerNodePath(serverIp, "status"));
        boolean disabled = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "disabled"));
        boolean paused = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "paused"));
        boolean shutdown = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "shutdown"));
        return ServerInfo.ServerStatus.getServerStatus(status, disabled, paused, shutdown);
    }
    
    @Override
    public Collection<ExecutionInfo> getExecutionInfo(final String jobName) {
        String executionRootPath = new JobNodePath(jobName).getExecutionNodePath();
        if (!regCenter.isExisted(executionRootPath)) {
            return Collections.emptyList();
        }
        List<String> items = regCenter.getChildrenKeys(executionRootPath);
        List<ExecutionInfo> result = new ArrayList<>(items.size());
        for (String each : items) {
            result.add(getExecutionInfo(jobName, each));
        }
        Collections.sort(result);
        return result;
    }
    
    private ExecutionInfo getExecutionInfo(final String jobName, final String item) {
        ExecutionInfo result = new ExecutionInfo();
        result.setItem(Integer.parseInt(item));
        JobNodePath jobNodePath = new JobNodePath(jobName);
        boolean running = regCenter.isExisted(jobNodePath.getExecutionNodePath(item, "running"));
        boolean completed = regCenter.isExisted(jobNodePath.getExecutionNodePath(item, "completed"));
        result.setStatus(ExecutionInfo.ExecutionStatus.getExecutionStatus(running, completed));
        if (regCenter.isExisted(jobNodePath.getExecutionNodePath(item, "failover"))) {
            result.setFailoverIp(regCenter.get(jobNodePath.getExecutionNodePath(item, "failover")));
        }
        String lastBeginTime = regCenter.get(jobNodePath.getExecutionNodePath(item, "lastBeginTime"));
        result.setLastBeginTime(null == lastBeginTime ? null : new Date(Long.parseLong(lastBeginTime)));
        String nextFireTime = regCenter.get(jobNodePath.getExecutionNodePath(item, "nextFireTime"));
        result.setNextFireTime(null == nextFireTime ? null : new Date(Long.parseLong(nextFireTime)));
        String lastCompleteTime = regCenter.get(jobNodePath.getExecutionNodePath(item, "lastCompleteTime"));
        result.setLastCompleteTime(null == lastCompleteTime ? null : new Date(Long.parseLong(lastCompleteTime)));
        return result;
    }
}
