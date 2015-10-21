/**
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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.plugin.job.type.AbstractSequenceDataFlowElasticJob;

/**
 * 不断获取最新数据并保证同一分片顺序性的处理数据流程作业.
 * 
 * <p>
 * 同一个分片用同一个线程处理, 保证顺序性和一致性, 类似kafka.
 * 作业执行过程中不断的获取是否有新数据.
 * 有新数据则作业一直不停止.
 * 适用于流式数据处理.
 * </p>
 * 
 * <p>
 * <strong>包结构调整, 作业类型全部迁移至plugin包. 未来版本将删除, 请从旧版本升级的程序升级.</strong>
 * </p>
 * @see com.dangdang.ddframe.job.plugin.job.type.AbstractSequenceDataFlowElasticJob
 * 
 * @author zhangliang
 * 
 * @param <T> 数据流作业处理的数据实体类型
 */
@Deprecated
public abstract class AbstractSequencePerpetualElasticJob<T> extends AbstractSequenceDataFlowElasticJob<T> {
    
    @Override
    public boolean isStreamingProcess() {
        return true;
    }
}
