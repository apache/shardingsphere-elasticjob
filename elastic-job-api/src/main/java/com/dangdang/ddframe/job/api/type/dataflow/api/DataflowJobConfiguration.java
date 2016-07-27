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

package com.dangdang.ddframe.job.api.type.dataflow.api;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.type.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据流作业配置信息.
 * 
 * @author caohao
 * @author zhangliang
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public final class DataflowJobConfiguration implements JobConfiguration {
    
    private final JobCoreConfiguration coreConfig;
    
    private final JobType jobType = JobType.DATAFLOW;
    
    private final Class<? extends DataflowJob> jobClass;
    
    private final DataflowType dataflowType;
    
    private final boolean streamingProcess;
    
    private int concurrentDataProcessThreadCount = Runtime.getRuntime().availableProcessors();
    
    /**
     * 数据流作业支持的处理类型.
     *
     * @author zhangliang
     */
    public enum DataflowType {
        
        THROUGHPUT, 
        SEQUENCE
    }
}
