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

package io.elasticjob.lite.lifecycle.api;

import io.elasticjob.lite.lifecycle.domain.JobBriefInfo;

import java.util.Collection;

/**
 * 作业状态展示的API.
 *
 * @author caohao
 */
public interface JobStatisticsAPI {
    
    /**
     * 获取作业总数.
     *
     * @return 作业总数.
     */
    int getJobsTotalCount();
    
    /**
     * 获取所有作业简明信息.
     *
     * @return 作业简明信息集合.
     */
    Collection<JobBriefInfo> getAllJobsBriefInfo();
    
    /**
     * 获取作业简明信息.
     *
     * @param jobName 作业名称
     * @return 作业简明信息.
     */
    JobBriefInfo getJobBriefInfo(String jobName);
    
    /**
     * 获取该IP下所有作业简明信息.
     *
     * @param ip 服务器IP
     * @return 作业简明信息集合.
     */
    Collection<JobBriefInfo> getJobsBriefInfo(String ip);
}
