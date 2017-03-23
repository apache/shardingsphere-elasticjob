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
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 作业操作的模板.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JobOperateTemplate {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * 作业操作.
     *
     * @param jobName 作业名称
     * @param serverIp 作业服务器IP地址
     * @param serverInstanceId 作业服务器实例ID
     * @param callback 作业操作的回调方法
     * @return 操作失败的作业服务器IP地址及实例ID列表(作业维度操作)或作业名称列表(IP维度操作)
     */
    public Collection<String> operate(final Optional<String> jobName, final Optional<String> serverIp, final Optional<String> serverInstanceId, final JobOperateCallback callback) {
        Preconditions.checkArgument(jobName.isPresent() || serverIp.isPresent(), "At least indicate jobName or serverIp.");
        Collection<String> result = Collections.emptyList();
        if (jobName.isPresent() && serverIp.isPresent() && serverInstanceId.isPresent()) {
            boolean isSuccess = callback.doOperate(jobName.get(), serverIp.get(), serverInstanceId.get());
            if (!isSuccess) {
                result = new ArrayList<>(1);
                result.add(serverIp.get() + "-" + serverInstanceId.get());
            }
        } else if (jobName.isPresent()) {
            JobNodePath jobNodePath = new JobNodePath(jobName.get());
            List<String> serverIpList = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
            result = new ArrayList<>(serverIpList.size());
            for (String ip : serverIpList) {
                for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath(ip))) {
                    boolean isSuccess = callback.doOperate(jobName.get(), ip, each);
                    if (!isSuccess) {
                        result.add(ip + "-" + each);
                    }
                }
            }
        } else {
            List<String> jobNames = regCenter.getChildrenKeys("/");
            result = new ArrayList<>(jobNames.size());
            for (String each : jobNames) {
                boolean isSuccess = callback.doOperate(each, serverIp.get(), serverInstanceId.get());
                if (!isSuccess) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}
