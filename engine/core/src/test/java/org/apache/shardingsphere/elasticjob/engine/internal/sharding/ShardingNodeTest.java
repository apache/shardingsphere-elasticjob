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

package org.apache.shardingsphere.elasticjob.engine.internal.sharding;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShardingNodeTest {
    
    private final ShardingNode shardingNode = new ShardingNode("test_job");
    
    @Test
    void assertGetRunningNode() {
        assertThat(ShardingNode.getRunningNode(0), is("sharding/0/running"));
    }
    
    @Test
    void assertGetMisfireNode() {
        assertThat(ShardingNode.getMisfireNode(0), is("sharding/0/misfire"));
    }
    
    @Test
    void assertGetItemWhenNotRunningItemPath() {
        assertNull(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/completed"));
    }
    
    @Test
    void assertGetItemByRunningItemPath() {
        assertThat(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/running"), is(0));
    }
}
