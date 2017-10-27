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

package io.elasticjob.lite.internal.sharding;

import io.elasticjob.lite.internal.election.LeaderNode;
import io.elasticjob.lite.internal.storage.JobNodePath;

/**
 * 分片节点路径.
 * 
 * @author zhangliang
 */
public final class ShardingNode {
    
    /**
     * 执行状态根节点.
     */
    public static final String ROOT = "sharding";
    
    static final String INSTANCE_APPENDIX = "instance";
    
    public static final String INSTANCE = ROOT + "/%s/" + INSTANCE_APPENDIX;
    
    static final String RUNNING_APPENDIX = "running";
    
    static final String RUNNING = ROOT + "/%s/" + RUNNING_APPENDIX;
    
    static final String MISFIRE = ROOT + "/%s/misfire";
    
    static final String DISABLED = ROOT + "/%s/disabled";
    
    static final String LEADER_ROOT = LeaderNode.ROOT + "/" + ROOT;
    
    static final String NECESSARY = LEADER_ROOT + "/necessary";
    
    static final String PROCESSING = LEADER_ROOT + "/processing";
    
    private final JobNodePath jobNodePath;
    
    public ShardingNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    public static String getInstanceNode(final int item) {
        return String.format(INSTANCE, item);
    }
    
    /**
     * 获取作业运行状态节点路径.
     *
     * @param item 作业项
     * @return 作业运行状态节点路径
     */
    public static String getRunningNode(final int item) {
        return String.format(RUNNING, item);
    }
    
    static String getMisfireNode(final int item) {
        return String.format(MISFIRE, item);
    }
    
    static String getDisabledNode(final int item) {
        return String.format(DISABLED, item);
    }
    
    /**
     * 根据运行中的分片路径获取分片项.
     *
     * @param path 运行中的分片路径
     * @return 分片项, 不是运行中的分片路径获则返回null
     */
    public Integer getItemByRunningItemPath(final String path) {
        if (!isRunningItemPath(path)) {
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ROOT).length() + 1, path.lastIndexOf(RUNNING_APPENDIX) - 1));
    }
    
    private boolean isRunningItemPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ROOT)) && path.endsWith(RUNNING_APPENDIX);
    }
}
