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

package com.dangdang.ddframe.job.lite.lifecycle.api;

import com.dangdang.ddframe.job.lite.lifecycle.domain.ExecutionInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;

import java.util.Collection;

/**
 * 作业状态展示的API.
 *
 * @author zhangliang
 */
public interface JobStatisticsAPI {
    
    /**
     * 获取作业简明信息.
     *
     * @param jobName 作业名称
     * @return 作业简明信息.
     */
    JobBriefInfo getJobBriefInfo(String jobName);
    
    /**
     * 获取所有作业简明信息.
     *
     * @return 作业简明信息集合.
     */
    Collection<JobBriefInfo> getAllJobsBriefInfo();
    
    /**
     * 获取执行作业的服务器.
     *
     * @param jobName 作业名称
     * @return 作业的服务器集合
     */
    Collection<ServerInfo> getServers(String jobName);
    
    /**
     * 获取作业运行时信息.
     *
     * @param jobName 作业名称
     * @return 作业运行时信息集合
     */
    Collection<ExecutionInfo> getExecutionInfo(String jobName);
}
