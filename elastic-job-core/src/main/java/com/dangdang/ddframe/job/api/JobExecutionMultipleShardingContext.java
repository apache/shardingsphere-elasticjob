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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.dangdang.ddframe.job.exception.JobException;

import lombok.Getter;
import lombok.Setter;

/**
 * 作业运行时多片分片上下文.
 * 
 * @author zhangliang
 */
@Getter
public final class JobExecutionMultipleShardingContext extends AbstractJobExecutionShardingContext {
    
    private static int initCollectionSize = 64;
    
    /**
     * 运行在本作业服务器的分片序列号集合.
     */
    @Setter
    private List<Integer> shardingItems = new ArrayList<>(initCollectionSize);
    
    /**
     * 运行在本作业项的分片序列号和个性化参数列表.
     */
    private Map<Integer, String> shardingItemParameters = new HashMap<>(initCollectionSize);
    
    /**
     * 根据分片项获取单分片作业运行时上下文.
     * 
     * @param item 分片项
     * @return 单分片作业运行时上下文
     */
    public JobExecutionSingleShardingContext createJobExecutionSingleShardingContext(final int item) {
        JobExecutionSingleShardingContext result = new JobExecutionSingleShardingContext();
        try {
            BeanUtils.copyProperties(result, this);
        } catch (final IllegalAccessException | InvocationTargetException ex) {
            throw new JobException(ex);
        }
        result.setShardingItem(item);
        result.setShardingItemParameter(shardingItemParameters.get(item));
        return result;
    }
}
