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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.StateNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Disable app node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DisableAppNode {
    
    static final String ROOT = StateNode.ROOT + "/disable/app";
    
    private static final String DISABLE_APP = ROOT + "/%s";
    
    static String getDisableAppNodePath(final String appName) {
        return String.format(DISABLE_APP, appName);
    }
}
