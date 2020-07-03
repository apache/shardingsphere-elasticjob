/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderNode;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;

/**
 * Sharding node.
 */
public final class ShardingNode {
    
    public static final String ROOT = "sharding";
    
    private static final String INSTANCE_APPENDIX = "instance";
    
    private static final String INSTANCE = ROOT + "/%s/" + INSTANCE_APPENDIX;
    
    private static final String RUNNING_APPENDIX = "running";
    
    private static final String RUNNING = ROOT + "/%s/" + RUNNING_APPENDIX;
    
    private static final String MISFIRE = ROOT + "/%s/misfire";
    
    private static final String DISABLED = ROOT + "/%s/disabled";
    
    private static final String LEADER_ROOT = LeaderNode.ROOT + "/" + ROOT;
    
    static final String NECESSARY = LEADER_ROOT + "/necessary";
    
    static final String PROCESSING = LEADER_ROOT + "/processing";
    
    private final JobNodePath jobNodePath;
    
    public ShardingNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }

    /**
     * Get the path of instance node.
     *
     * @param item sharding item
     * @return the path of instance node
     */
    public static String getInstanceNode(final int item) {
        return String.format(INSTANCE, item);
    }
    
    /**
     * Get job running node.
     *
     * @param item sharding item
     * @return job running node
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
     * Get item by running item path.
     *
     * @param path running item path
     * @return running item, return null if sharding item is not running
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
