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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.domain;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingStatusTest {
    
    @Test
    public void assertGetShardingStatusWhenIsDisabled() {
        assertThat(ShardingInfo.ShardingStatus.getShardingStatus(true, false, true), is(ShardingInfo.ShardingStatus.DISABLED));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsRunning() {
        assertThat(ShardingInfo.ShardingStatus.getShardingStatus(false, true, false), is(ShardingInfo.ShardingStatus.RUNNING));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsPending() {
        assertThat(ShardingInfo.ShardingStatus.getShardingStatus(false, false, false), is(ShardingInfo.ShardingStatus.PENDING));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsShardingError() {
        assertThat(ShardingInfo.ShardingStatus.getShardingStatus(false, false, true), is(ShardingInfo.ShardingStatus.SHARDING_FLAG));
    }
}
