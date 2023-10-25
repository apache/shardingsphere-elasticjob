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

package org.apache.shardingsphere.elasticjob.lifecycle.internal.operate;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.elasticjob.infra.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.kernel.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.io.IOException;
import java.util.List;

/**
 * Job operate API implementation class.
 */
public final class JobOperateAPIImpl implements JobOperateAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    public JobOperateAPIImpl(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    @Override
    public void trigger(final String jobName) {
        Preconditions.checkNotNull(jobName, "Job name cannot be null");
        new InstanceService(regCenter, jobName).triggerAllInstances();
    }
    
    @Override
    public void disable(final String jobName, final String serverIp) {
        disableOrEnableJobs(jobName, serverIp, true);
    }
    
    @Override
    public void enable(final String jobName, final String serverIp) {
        disableOrEnableJobs(jobName, serverIp, false);
    }
    
    private void disableOrEnableJobs(final String jobName, final String serverIp, final boolean disabled) {
        Preconditions.checkArgument(null != jobName || null != serverIp, "At least indicate jobName or serverIp.");
        if (null != jobName && null != serverIp) {
            persistDisabledOrEnabledJob(jobName, serverIp, disabled);
        } else if (null != jobName) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
                if (disabled) {
                    regCenter.persist(jobNodePath.getServerNodePath(each), ServerStatus.DISABLED.name());
                } else {
                    regCenter.persist(jobNodePath.getServerNodePath(each), ServerStatus.ENABLED.name());
                }
            }
        } else {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String each : jobNames) {
                if (regCenter.isExisted(new JobNodePath(each).getServerNodePath(serverIp))) {
                    persistDisabledOrEnabledJob(each, serverIp, disabled);
                }
            }
        }
    }
    
    private void persistDisabledOrEnabledJob(final String jobName, final String serverIp, final boolean disabled) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String serverNodePath = jobNodePath.getServerNodePath(serverIp);
        if (disabled) {
            regCenter.persist(serverNodePath, ServerStatus.DISABLED.name());
        } else {
            regCenter.persist(serverNodePath, ServerStatus.ENABLED.name());
        }
    }
    
    @Override
    public void shutdown(final String jobName, final String serverIp) {
        Preconditions.checkArgument(null != jobName || null != serverIp, "At least indicate jobName or serverIp.");
        if (null != jobName && null != serverIp) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                JobInstance jobInstance = YamlEngine.unmarshal(regCenter.get(jobNodePath.getInstanceNodePath(each)), JobInstance.class);
                if (serverIp.equals(jobInstance.getServerIp())) {
                    regCenter.remove(jobNodePath.getInstanceNodePath(each));
                }
            }
        } else if (null != jobName) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                regCenter.remove(jobNodePath.getInstanceNodePath(each));
            }
        } else {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String job : jobNames) {
                JobNodePath jobNodePath = new JobNodePath(job);
                List<String> instances = regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath());
                for (String each : instances) {
                    JobInstance jobInstance = YamlEngine.unmarshal(regCenter.get(jobNodePath.getInstanceNodePath(each)), JobInstance.class);
                    if (serverIp.equals(jobInstance.getServerIp())) {
                        regCenter.remove(jobNodePath.getInstanceNodePath(each));
                    }
                }
            }
        }
    }
    
    @Override
    public void remove(final String jobName, final String serverIp) {
        shutdown(jobName, serverIp);
        if (null != jobName && null != serverIp) {
            regCenter.remove(new JobNodePath(jobName).getServerNodePath(serverIp));
        } else if (null != jobName) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
            for (String each : servers) {
                regCenter.remove(jobNodePath.getServerNodePath(each));
            }
        } else if (null != serverIp) {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            for (String each : jobNames) {
                regCenter.remove(new JobNodePath(each).getServerNodePath(serverIp));
            }
        }
    }
    
    @Override
    public String dump(final String jobName, final String instanceIp, final int dumpPort) throws IOException {
        return SnapshotService.dumpJob(instanceIp, dumpPort, jobName);
    }
}
