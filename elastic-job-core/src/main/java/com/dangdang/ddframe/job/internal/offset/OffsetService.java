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

package com.dangdang.ddframe.job.internal.offset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Strings;

/**
 * 数据处理位置服务.
 * 
 * @author zhangliang
 */
public class OffsetService {
    
    private final JobNodeStorage jobNodeStorage;
    
    public OffsetService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 更新数据处理位置.
     * 
     * @param item 分片项
     * @param offset 数据处理位置
     */
    public void updateOffset(final int item, final String offset) {
        String node = OffsetNode.getItemNode(item);
        jobNodeStorage.createJobNodeIfNeeded(node);
        jobNodeStorage.updateJobNode(node, offset);
    }
    
    /**
     * 获取数据分片项和数据处理位置Map.
     * 
     * @param items 分片项集合
     * @return 数据分片项和数据处理位置Map
     */
    public Map<Integer, String> getOffsets(final List<Integer> items) {
        Map<Integer, String> result = new HashMap<>(items.size());
        for (int each : items) {
            String offset = jobNodeStorage.getJobNodeDataDirectly(OffsetNode.getItemNode(each));
            if (!Strings.isNullOrEmpty(offset)) {
                result.put(each, offset);
            }
        }
        return result;
    }
}
