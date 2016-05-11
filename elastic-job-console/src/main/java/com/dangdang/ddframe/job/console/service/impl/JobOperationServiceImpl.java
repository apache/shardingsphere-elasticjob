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

package com.dangdang.ddframe.job.console.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.dangdang.ddframe.job.console.repository.zookeeper.CuratorRepository;
import com.dangdang.ddframe.job.console.service.JobOperationService;
import com.dangdang.ddframe.job.console.util.JobNodePath;

@Service
public class JobOperationServiceImpl implements JobOperationService {
    
    @Resource
    private CuratorRepository curatorRepository;
    
    @Override
    public void stopJob(final String jobName, final String serverIp) {
        curatorRepository.create(JobNodePath.getServerNodePath(jobName, serverIp, "stoped"));
    }
    
    @Override
    public void resumeJob(final String jobName, final String serverIp) {
        curatorRepository.delete(JobNodePath.getServerNodePath(jobName, serverIp, "stoped"));
    }
    
    @Override
    public void stopAllJobsByJobName(final String jobName) {
        for (String each : curatorRepository.getChildren(JobNodePath.getServerNodePath(jobName))) {
            curatorRepository.create(JobNodePath.getServerNodePath(jobName, each, "stoped"));
        }
    }
    
    @Override
    public void resumeAllJobsByJobName(final String jobName) {
        for (String each : curatorRepository.getChildren(JobNodePath.getServerNodePath(jobName))) {
            curatorRepository.delete(JobNodePath.getServerNodePath(jobName, each, "stoped"));
        }
    }
    
    @Override
    public void stopAllJobsByServer(final String serverIp) {
        for (String jobName : curatorRepository.getChildren("/")) {
            String leaderIp = curatorRepository.getData(JobNodePath.getLeaderNodePath(jobName, "election/host"));
            if (serverIp.equals(leaderIp)) {
                for (String toBeStopedIp : curatorRepository.getChildren(JobNodePath.getServerNodePath(jobName))) {
                    curatorRepository.create(JobNodePath.getServerNodePath(jobName, toBeStopedIp, "stoped"));
                }
            } else {
                curatorRepository.create(JobNodePath.getServerNodePath(jobName, serverIp, "stoped"));
            }
        }
    }
    
    @Override
    public void resumeAllJobsByServer(final String serverIp) {
        for (String jobName : curatorRepository.getChildren("/")) {
            String leaderIp = curatorRepository.getData(JobNodePath.getLeaderNodePath(jobName, "election/host"));
            if (!serverIp.equals(leaderIp) && curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, leaderIp, "stoped"))) {
                continue;
            }
            curatorRepository.delete(JobNodePath.getServerNodePath(jobName, serverIp, "stoped"));
        }
    }
    
    @Override
    public void shutdownJob(final String jobName, final String serverIp) {
        curatorRepository.create(JobNodePath.getServerNodePath(jobName, serverIp, "shutdown"));
    }
    
    @Override
    public boolean removeJob(final String jobName, final String serverIp) {
        if (!curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, serverIp, "status")) || curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, serverIp, "shutdown"))) {
            curatorRepository.delete(JobNodePath.getServerNodePath(jobName, serverIp));
            return true;
        }
        return false;
    }

    @Override
    public void disableJob(final String jobName, final String serverIp) {
        curatorRepository.create(JobNodePath.getServerNodePath(jobName, serverIp, "disabled"));
    }

    @Override
    public void enableJob(final String jobName, final String serverIp) {
        curatorRepository.delete(JobNodePath.getServerNodePath(jobName, serverIp, "disabled"));
    }
}
