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

package com.dangdang.ddframe.job.cloud.mesos.stragety;

import com.dangdang.ddframe.job.cloud.job.config.CloudJobConfiguration;
import org.apache.mesos.Protos;

import java.util.Collection;
import java.util.List;

/**
 * 资源分配策略接口.
 *
 * @author zhangliang
 */
public interface ResourceAllocateStrategy {
    
    /**
     * 分配资源.
     * 
     * @param jobConfig 云作业配置
     * @return 分配是否成功
     */
    boolean allocate(CloudJobConfiguration jobConfig);
    
    /**
     * 分配资源.
     *
     * @param jobConfig 云作业配置
     * @param shardingItems 分片项列表
     * @return 分配是否成功
     */
    boolean allocate(CloudJobConfiguration jobConfig, List<Integer> shardingItems);
    
    /**
     * 获取任务列表.
     * 
     * @return 任务列表
     */
    List<Protos.TaskInfo> getTaskInfoList();
}
