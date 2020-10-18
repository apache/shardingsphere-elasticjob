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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Job statistics API implementation class.
 */
@RequiredArgsConstructor
public final class JobStatisticsAPIImpl implements JobStatisticsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public int getJobsTotalCount() {
        return regCenter.getChildrenKeys("/").size();
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
    public JobBriefInfo getJobBriefInfo(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        String jobConfigYaml = regCenter.get(jobNodePath.getConfigNodePath());
        if (null == jobConfigYaml) {
            return null;
        }
        JobConfiguration jobConfig = YamlEngine.unmarshal(jobConfigYaml, JobConfigurationPOJO.class).toJobConfiguration();
        result.setDescription(jobConfig.getDescription());
        result.setCron(jobConfig.getCron());
        result.setInstanceCount(getJobInstanceCount(jobName));
        result.setShardingTotalCount(jobConfig.getShardingTotalCount());
        result.setStatus(getJobStatus(jobName));
        return result;
    }
    
    private JobBriefInfo.JobStatus getJobStatus(final String jobName) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        List<String> instances = regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath());
        if (instances.isEmpty()) {
            return JobBriefInfo.JobStatus.CRASHED;
        }
        if (isAllDisabled(jobNodePath)) {
            return JobBriefInfo.JobStatus.DISABLED;
        }
        if (isHasShardingFlag(jobNodePath, instances)) {
            return JobBriefInfo.JobStatus.SHARDING_FLAG;
        }
        return JobBriefInfo.JobStatus.OK;
    }
    
    private boolean isAllDisabled(final JobNodePath jobNodePath) {
        List<String> serversPath = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
        int disabledServerCount = 0;
        for (String each : serversPath) {
            if (JobBriefInfo.JobStatus.DISABLED.name().equals(regCenter.get(jobNodePath.getServerNodePath(each)))) {
                disabledServerCount++;
            }
        }
        return disabledServerCount == serversPath.size();
    }
    
    private boolean isHasShardingFlag(final JobNodePath jobNodePath, final List<String> instances) {
        Set<String> shardingInstances = new HashSet<>();
        for (String each : regCenter.getChildrenKeys(jobNodePath.getShardingNodePath())) {
            String instanceId = regCenter.get(jobNodePath.getShardingNodePath(each, "instance"));
            if (null != instanceId && !instanceId.isEmpty()) {
                shardingInstances.add(instanceId);
            }
        }
        return !instances.containsAll(shardingInstances) || shardingInstances.isEmpty();
    }
    
    private int getJobInstanceCount(final String jobName) {
        return regCenter.getChildrenKeys(new JobNodePath(jobName).getInstancesNodePath()).size();
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
        if (!regCenter.isExisted(new JobNodePath(jobName).getServerNodePath(ip))) {
            return null;
        }
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        result.setStatus(getJobStatusByJobNameAndIp(jobName, ip));
        result.setInstanceCount(getJobInstanceCountByJobNameAndIP(jobName, ip));
        return result;
    }
    
    private JobBriefInfo.JobStatus getJobStatusByJobNameAndIp(final String jobName, final String ip) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String status = regCenter.get(jobNodePath.getServerNodePath(ip));
        if ("DISABLED".equalsIgnoreCase(status)) {
            return JobBriefInfo.JobStatus.DISABLED;
        } else {
            return JobBriefInfo.JobStatus.OK;
        }
    }
    
    private int getJobInstanceCountByJobNameAndIP(final String jobName, final String ip) {
        int result = 0;
        JobNodePath jobNodePath = new JobNodePath(jobName);
        List<String> instances = regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath());
        for (String each : instances) {
            if (ip.equals(each.split("@-@")[0])) {
                result++;
            }
        }
        return result;
    }
}
