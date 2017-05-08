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

package com.dangdang.ddframe.job.lite.lifecycle.domain;

import com.dangdang.ddframe.job.lite.lifecycle.domain.ShardingInfo.ShardingStatus;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ShardingStatusTest {
    
    @Test
    public void assertGetShardingStatusWhenIsDisabled() {
        assertThat(ShardingStatus.getShardingStatus(true, false,  true), is(ShardingStatus.DISABLED));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsRunning() {
        assertThat(ShardingStatus.getShardingStatus(false, true,  false), is(ShardingStatus.RUNNING));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsPending() {
        assertThat(ShardingStatus.getShardingStatus(false, false,  false), is(ShardingStatus.PENDING));
    }
    
    @Test
    public void assertGetShardingStatusWhenIsShardingError() {
        assertThat(ShardingStatus.getShardingStatus(false, false,  true), is(ShardingStatus.SHARDING_ERROR));
    }
}
