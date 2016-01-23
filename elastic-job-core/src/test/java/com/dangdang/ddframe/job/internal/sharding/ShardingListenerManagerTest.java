/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.sharding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.test.WaitingUtils;

public final class ShardingListenerManagerTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new LocalHostService();
    
    private final ShardingListenerManager shardingListenerManager = new ShardingListenerManager(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        shardingListenerManager.listenShardingTotalCountChanged();
        shardingListenerManager.listenServersChanged();
    }
    
    @Test
    public void assertListenShardingTotalCountChanged() {
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/execution/necessary"));
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "1");
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/execution/necessary"));
    }
    
    @Test
    public void assertListenServersChangedWhenServerIsCrashed() {
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
    }
    
    @Test
    public void assertListenServersChangedWhenServerIsNotCrashed() {
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/leader/sharding/necessary");
        getRegistryCenter().update("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.RUNNING.name());
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
    }
    
    @Test
    public void assertListenServersChangedWhenServerIsDisabled() {
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/disabled", "");
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
    }
}
