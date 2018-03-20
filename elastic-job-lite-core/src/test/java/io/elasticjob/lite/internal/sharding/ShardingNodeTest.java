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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ShardingNodeTest {
    
    private ShardingNode shardingNode = new ShardingNode("test_job");
    
    @Test
    public void assertGetRunningNode() {
        assertThat(ShardingNode.getRunningNode(0), is("sharding/0/running"));
    }
    
    @Test
    public void assertGetMisfireNode() {
        assertThat(ShardingNode.getMisfireNode(0), is("sharding/0/misfire"));
    }
    
    @Test
    public void assertGetItemWhenNotRunningItemPath() {
        assertNull(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/completed"));
    }
    
    @Test
    public void assertGetItemByRunningItemPath() {
        assertThat(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/running"), is(0));
    }
}
