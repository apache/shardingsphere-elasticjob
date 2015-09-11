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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.test.WaitingUtils;

public final class ShardingServiceTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final ShardingService shardingService = new ShardingService(getRegistryCenter(), getJobConfig());
    
    @Test
    public void assertSetReshardingFlag() {
        shardingService.setReshardingFlag();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
    }
    
    @Test
    public void assertIsNotNeedSharding() {
        assertFalse(shardingService.isNeedSharding());
    }
    
    @Test
    public void assertIsNeedSharding() {
        getRegistryCenter().persist("/testJob/leader/sharding/necessary", "");
        assertTrue(shardingService.isNeedSharding());
    }
    
    @Test
    public void assertShardingWhenUnnecessary() {
        shardingService.shardingIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/servers"));
    }
    
    @Test
    public void assertShardingWhenIsNotLeaderAndIsShardingProcessing() {
        getRegistryCenter().persist("/testJob/leader/sharding/necessary", "");
        getRegistryCenter().persistEphemeral("/testJob/leader/election/host", "host0");
        getRegistryCenter().persistEphemeral("/testJob/leader/sharding/processing", "");
        new Thread() {
            
            @Override
            public void run() {
                WaitingUtils.waitingShortTime();
                getRegistryCenter().remove("/testJob/leader/sharding/processing");
                getRegistryCenter().remove("/testJob/leader/sharding/necessary");
            }
        }.start();
        shardingService.shardingIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/servers/sharding"));
    }
    
    @Test
    public void assertShardingNecessary() {
        String localHostIp = localHostService.getIp();
        getRegistryCenter().persist("/testJob/leader/sharding/necessary", "");
        getRegistryCenter().persistEphemeral("/testJob/leader/election/host", localHostIp);
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/servers/" + localHostIp + "/status", ServerStatus.READY.name());
        getRegistryCenter().persistEphemeral("/testJob/leader/sharding/processing", "");
        shardingService.shardingIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/processing"));
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostIp + "/sharding"), is("0,1,2"));
    }
    
    @Test
    public void assertGetLocalHostShardingItemsWhenNodeExisted() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0,1,2");
        assertThat(shardingService.getLocalHostShardingItems(), is(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertGetLocalHostShardingWhenNodeNotExisted() {
        assertThat(shardingService.getLocalHostShardingItems(), is(Collections.EMPTY_LIST));
    }
}
