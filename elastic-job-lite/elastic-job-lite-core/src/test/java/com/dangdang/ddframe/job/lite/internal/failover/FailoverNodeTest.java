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

package com.dangdang.ddframe.job.lite.internal.failover;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class FailoverNodeTest {
    
    private FailoverNode failoverNode = new FailoverNode("test_job");
    
    @Test
    public void assertGetItemsNode() {
        assertThat(FailoverNode.getItemsNode(0), is("leader/failover/items/0"));
    }
    
    @Test
    public void assertGetExecutionFailoverNode() {
        assertThat(FailoverNode.getExecutionFailoverNode(0), is("sharding/0/failover"));
    }
    
    @Test
    public void assertGetItemWhenNotExecutionFailoverPath() {
        assertNull(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/completed"));
    }
    
    @Test
    public void assertGetItemByExecutionFailoverPath() {
        assertThat(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/failover"), is(0));
    }
}
