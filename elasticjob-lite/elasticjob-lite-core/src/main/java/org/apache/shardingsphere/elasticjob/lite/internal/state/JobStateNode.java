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

package org.apache.shardingsphere.elasticjob.lite.internal.state;

import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;

/**
 * Job state state node path.
 *
 **/
public class JobStateNode {
    public static final String ROOT_STATE = "state/state";

    public static final String ROOT_STATE_FOR_CACHE = "state";

    private static final String ROOT_PROC = "proc";

    private static final String ROOT_FAIL = "proc/fail";

    private static final String ROOT_SUCC = "proc/succ";

    private static final String PROC_FAIL = "proc/fail/%s";

    private static final String PROC_SUCC = "proc/succ/%s";

    private final JobNodePath jobNodePath;

    public JobStateNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }

    /**
     * Get job's root state path.
     *
     * @return root state path.
     */
    public static String getRootState() {
        return ROOT_STATE;
    }

    /**
     * Get job's proc path.
     *
     * @return proc path
     */
    public static String getRootProc() {
        return ROOT_PROC;
    }

    /**
     * Get job item's fail path.
     *
     * @param item sharding item
     * @return fail path.
     */
    public static String getProcFail(final int item) {
        return String.format(PROC_FAIL, item);
    }

    /**
     * Get job item's success path.
     * @param item sharding item.
     * @return success path.
     */
    public static String getProcSucc(final int item) {
        return String.format(PROC_SUCC, item);
    }

    /**
     * Get job proc fail path.
     *
     * @return proc fail path
     */
    public static String getRooProcFail() {
        return ROOT_FAIL;
    }

    /**
     * Get job proc success path.
     *
     * @return proc success path
     */
    public static String getRooProcSucc() {
        return ROOT_SUCC;
    }

}
