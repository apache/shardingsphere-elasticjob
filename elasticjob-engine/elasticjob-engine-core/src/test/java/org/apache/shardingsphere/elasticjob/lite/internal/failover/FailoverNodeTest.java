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

package org.apache.shardingsphere.elasticjob.lite.internal.failover;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class FailoverNodeTest {
    
    private final FailoverNode failoverNode = new FailoverNode("test_job");
    
    @Test
    void assertGetItemsNode() {
        assertThat(FailoverNode.getItemsNode(0), is("leader/failover/items/0"));
    }
    
    @Test
    void assertGetExecutionFailoverNode() {
        assertThat(FailoverNode.getExecutionFailoverNode(0), is("sharding/0/failover"));
    }
    
    @Test
    void assertGetItemWhenNotExecutionFailoverPath() {
        assertNull(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/completed"));
    }
    
    @Test
    void assertGetItemByExecutionFailoverPath() {
        assertThat(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/failover"), is(0));
    }
    
    @Test
    void assertGetProcessingFailoverNode() {
        assertThat(FailoverNode.getExecutingFailoverNode(0), is("sharding/0/failovering"));
    }
}
