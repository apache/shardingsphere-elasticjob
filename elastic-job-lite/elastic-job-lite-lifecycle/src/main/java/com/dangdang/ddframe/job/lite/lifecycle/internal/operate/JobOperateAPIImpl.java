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

package com.dangdang.ddframe.job.lite.lifecycle.internal.operate;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * 操作作业的实现类.
 *
 * @author caohao
 */
public final class JobOperateAPIImpl implements JobOperateAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    public JobOperateAPIImpl(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    @Override
    public void trigger(final Optional<String> jobName, final Optional<String> serverIp) {
        if (jobName.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                regCenter.persist(jobNodePath.getInstanceNodePath(each), "TRIGGER");
            }
        }
    }
    
    @Override
    public void disable(final Optional<String> jobName, final Optional<String> serverIp) {
        disableOrEnableJobs(jobName, serverIp, true);
    }
    
    @Override
    public void enable(final Optional<String> jobName, final Optional<String> serverIp) {
        disableOrEnableJobs(jobName, serverIp, false);
    }
    
    private void disableOrEnableJobs(final Optional<String> jobName, final Optional<String> serverIp, final boolean disabled) {
        Preconditions.checkArgument(jobName.isPresent() || serverIp.isPresent(), "At least indicate jobName or serverIp.");
        if (jobName.isPresent() && serverIp.isPresent()) {
            persistDisabledOrEnabledJob(jobName.get(), serverIp.get(), disabled);
        } else if (jobName.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
                if (disabled) {
                    regCenter.persist(jobNodePath.getServerNodePath(each), "DISABLED");
                } else {
                    regCenter.persist(jobNodePath.getServerNodePath(each), "");
                }
            }
        } else if (serverIp.isPresent()) {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String each : jobNames) {
                if (regCenter.isExisted(new JobNodePath(each).getServerNodePath(serverIp.get()))) {
                    persistDisabledOrEnabledJob(each, serverIp.get(), disabled);
                }
            }
        }
    }
    
    private void persistDisabledOrEnabledJob(final String jobName, final String serverIp, final boolean disabled) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String serverNodePath = jobNodePath.getServerNodePath(serverIp);
        if (disabled) {
            regCenter.persist(serverNodePath, "DISABLED");
        } else {
            regCenter.persist(serverNodePath, "");
        }
    }
    
    @Override
    public void shutdown(final Optional<String> jobName, final Optional<String> serverIp) {
        Preconditions.checkArgument(jobName.isPresent() || serverIp.isPresent(), "At least indicate jobName or serverIp.");
        if (jobName.isPresent() && serverIp.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                if (serverIp.get().equals(each.split("@-@")[0])) {
                    regCenter.remove(jobNodePath.getInstanceNodePath(each));
                }
            }
        } else if (jobName.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                regCenter.remove(jobNodePath.getInstanceNodePath(each));
            }
        } else if (serverIp.isPresent()) {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String job : jobNames) {
                JobNodePath jobNodePath = new JobNodePath(job);
                List<String> instances = regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath());
                for (String each : instances) {
                    if (serverIp.get().equals(each.split("@-@")[0])) {
                        regCenter.remove(jobNodePath.getInstanceNodePath(each));
                    }
                }
            }
        }
    }
    
    @Override
    public void remove(final Optional<String> jobName, final Optional<String> serverIp) {
        shutdown(jobName, serverIp);
        if (jobName.isPresent() && serverIp.isPresent()) {
            regCenter.remove(new JobNodePath(jobName.get()).getServerNodePath(serverIp.get()));
        } else if (jobName.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
            for (String each : servers) {
                regCenter.remove(jobNodePath.getServerNodePath(each));
            }
        } else if (serverIp.isPresent()) {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String each : jobNames) {
                regCenter.remove(new JobNodePath(each).getServerNodePath(serverIp.get()));
            }
        }
    }
}
