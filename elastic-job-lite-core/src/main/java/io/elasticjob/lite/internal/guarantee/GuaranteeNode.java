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

package io.elasticjob.lite.internal.guarantee;

import io.elasticjob.lite.internal.storage.JobNodePath;
import com.google.common.base.Joiner;

/**
 * 保证分布式任务全部开始和结束状态节点路径.
 * 
 * @author zhangliang
 */
public final class GuaranteeNode {
    
    static final String ROOT = "guarantee";
    
    static final String STARTED_ROOT = ROOT + "/started";
    
    static final String COMPLETED_ROOT = ROOT + "/completed";
    
    private final JobNodePath jobNodePath;
    
    GuaranteeNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    static String getStartedNode(final int shardingItem) {
        return Joiner.on("/").join(STARTED_ROOT, shardingItem);
    }
    
    static String getCompletedNode(final int shardingItem) {
        return Joiner.on("/").join(COMPLETED_ROOT, shardingItem);
    }
    
    boolean isStartedRootNode(final String path) {
        return jobNodePath.getFullPath(STARTED_ROOT).equals(path);
    }
    
    boolean isCompletedRootNode(final String path) {
        return jobNodePath.getFullPath(COMPLETED_ROOT).equals(path);
    }
}
