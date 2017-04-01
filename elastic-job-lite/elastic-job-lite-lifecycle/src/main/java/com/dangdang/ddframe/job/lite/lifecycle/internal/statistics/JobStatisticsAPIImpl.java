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
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo.JobStatus;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ShardingInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ShardingInfo.ShardingStatus;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        result.setCron(liteJobConfig.getTypeConfig().getCoreConfig().getCron());
        result.setShardingItems(getJobShardingItems(jobName));
        result.setStatus(getJobStatus(jobName));
        return result;
    }
    
    private JobStatus getJobStatus(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
        if (servers.isEmpty()) {
            return JobStatus.CRASHED;
        }
        for (String each : servers) {
            String status = regCenter.get(jobNodePath.getServerNodePath(each)); 
            if ("DISABLED".equalsIgnoreCase(status)) {
                return JobStatus.DISABLED;
            }
        }
        return JobStatus.OK;
    }
    
    private String getJobShardingItems(final String jobName) {
        List<String> shardingItems = regCenter.getChildrenKeys(new JobNodePath(jobName).getShardingNodePath());
        Collections.sort(shardingItems);
        return Joiner.on(",").join(shardingItems);
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
    
    @Override
    public Collection<JobBriefInfo> getJobsBriefInfo(final String ip) {
        List<String> jobNames = regCenter.getChildrenKeys("/");
        List<JobBriefInfo> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            JobBriefInfo jobBriefInfo = getJobBriefInfoByJobNameAndIp(each, ip);
            if (null != jobBriefInfo) {
                result.add(jobBriefInfo);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    private JobBriefInfo getJobBriefInfoByJobNameAndIp(final String jobName, final String ip) {
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        result.setStatus(getJobStatusByJobNameAndIp(jobName, ip));
        result.setShardingItems(getJobShardingItems(jobName));
        return result;
    }
    
    private JobStatus getJobStatusByJobNameAndIp(final String jobName, final String ip) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String status = regCenter.get(jobNodePath.getServerNodePath(ip));
        if ("DISABLED".equalsIgnoreCase(status)) {
            return JobStatus.DISABLED;
        } else {
            return JobStatus.OK;
        }
    }
    
    @Override
    public Collection<ShardingInfo> getShardingInfo(final String jobName) {
        String executionRootPath = new JobNodePath(jobName).getShardingNodePath();
        if (!regCenter.isExisted(executionRootPath)) {
            return Collections.emptyList();
        }
        List<String> items = regCenter.getChildrenKeys(executionRootPath);
        List<ShardingInfo> result = new ArrayList<>(items.size());
        for (String each : items) {
            result.add(getShardingInfo(jobName, each));
        }
        Collections.sort(result);
        return result;
    }
    
    private ShardingInfo getShardingInfo(final String jobName, final String item) {
        ShardingInfo result = new ShardingInfo();
        result.setItem(Integer.parseInt(item));
        JobNodePath jobNodePath = new JobNodePath(jobName);
        boolean running = regCenter.isExisted(jobNodePath.getShardingNodePath(item, "running"));
        boolean completed = regCenter.isExisted(jobNodePath.getShardingNodePath(item, "completed"));
        result.setFailover(regCenter.isExisted(jobNodePath.getShardingNodePath(item, "failover")));
        result.setStatus(ShardingStatus.getShardingStatus(running, completed));
        return result;
    }
}
