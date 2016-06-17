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

package com.dangdang.ddframe.job.internal.operate;

import com.dangdang.ddframe.job.api.JobOperateAPI;
import com.dangdang.ddframe.job.internal.storage.JobNodePath;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.Collection;

/**
 * 操作作业的实现类.
 *
 * @author zhangliang
 */
public final class JobOperateAPIImpl implements JobOperateAPI {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final JobOperateTemplate jobOperatorTemplate;
    
    public JobOperateAPIImpl(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        jobOperatorTemplate = new JobOperateTemplate(registryCenter);
    }
    
    @Override
    public void trigger(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.persist(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.TRIGGER_NODE), "");
                return true;
            }
        });
    }
    
    @Override
    public void pause(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.persist(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.PAUSED_NODE), "");
                return true;
            }
        });
    }
    
    @Override
    public void resume(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
        
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.remove(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.PAUSED_NODE));
                return true;
            }
        });
    }
    
    @Override
    public void disable(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.persist(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.DISABLED_NODE), "");
                return true;
            }
        });
    }
    
    @Override
    public void enable(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.remove(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.DISABLED_NODE));
                return true;
            }
        });
    }
    
    @Override
    public void shutdown(final Optional<String> jobName, final Optional<String> serverIp) {
        jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                registryCenter.persist(new JobNodePath(jobName).getServerNodePath(serverIp, JobNodePath.SHUTDOWN_NODE), "");
                return true;
            }
        });
    }
    
    @Override
    public Collection<String> remove(final Optional<String> jobName, final Optional<String> serverIp) {
        return jobOperatorTemplate.operate(jobName, serverIp, new JobOperateCallback() {
            
            @Override
            public boolean doOperate(final String jobName, final String serverIp) {
                JobNodePath jobNodePath = new JobNodePath(jobName);
                if (registryCenter.isExisted(jobNodePath.getServerNodePath(serverIp, JobNodePath.STATUS_NODE))) {
                    return false;
                }
                registryCenter.remove(jobNodePath.getServerNodePath(serverIp));
                if (registryCenter.getChildrenKeys(jobNodePath.getServerNodePath()).isEmpty()) {
                    registryCenter.remove("/" + jobName);
                }
                return true;
            }
        });
    }
}
