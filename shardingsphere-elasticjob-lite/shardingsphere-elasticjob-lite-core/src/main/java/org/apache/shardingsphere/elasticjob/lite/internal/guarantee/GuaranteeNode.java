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

package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;

/**
 * Guarantee node.
 */
public final class GuaranteeNode {
    
    private static final String ROOT = "guarantee";
    
    static final String STARTED_ROOT = ROOT + "/started";
    
    static final String COMPLETED_ROOT = ROOT + "/completed";
    
    private final JobNodePath jobNodePath;
    
    GuaranteeNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    static String getStartedNode(final int shardingItem) {
        return String.join("/", STARTED_ROOT, shardingItem + "");
    }
    
    static String getCompletedNode(final int shardingItem) {
        return String.join("/", COMPLETED_ROOT, shardingItem + "");
    }
    
    boolean isStartedRootNode(final String path) {
        return jobNodePath.getFullPath(STARTED_ROOT).equals(path);
    }
    
    boolean isCompletedRootNode(final String path) {
        return jobNodePath.getFullPath(COMPLETED_ROOT).equals(path);
    }
}
