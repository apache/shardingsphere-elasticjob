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

package com.dangdang.ddframe.job.internal.failover;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;

public final class FailoverServiceTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final FailoverService failoverService = new FailoverService(getRegistryCenter(), getJobConfig());
    
    @Test
    public void assertSetCrashedFailoverFlag() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0");
        failoverService.setCrashedFailoverFlag(0);
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsAssigned() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0");
        getRegistryCenter().persist("/testJob/execution/0/failover", "host0");
        failoverService.setCrashedFailoverFlag(0);
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeNotExisted() {
        failoverService.failoverIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/latch"));
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeIsEmpty() {
        getRegistryCenter().persist("/testJob/leader/failover/items", "");
        failoverService.failoverIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/latch"));
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenServerIsNotReady() {
        getRegistryCenter().persist("/testJob/leader/failover/items/0", "");
        failoverService.failoverIfNecessary();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/latch"));
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        getRegistryCenter().persist("/testJob/leader/failover/items/0", "");
        getRegistryCenter().persist("/testJob/leader/failover/items/1", "");
        JobScheduler jobScheduler = new JobScheduler(getRegistryCenter(), getJobConfig());
        jobScheduler.init();
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        failoverService.failoverIfNecessary();
        jobScheduler.stopJob();
        jobScheduler.shutdown();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/failover/latch"));
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/1"));
        assertThat(getRegistryCenter().get("/testJob/execution/1/failover"), is(localHostService.getIp()));
    }
    
    @Test
    public void assertUpdateFailoverComplete() {
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        failoverService.updateFailoverComplete(Arrays.asList(0));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/failover"));
    }
    
    @Test
    public void assertGetLocalHostFailoverItems() {
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        getRegistryCenter().persist("/testJob/execution/1/failover", localHostService.getIp());
        getRegistryCenter().persist("/testJob/execution/2/failover", "host0");
        assertThat(failoverService.getLocalHostFailoverItems(), is(Arrays.asList(0, 1)));
    }
    
    @Test
    public void assertGetLocalHostTakeOffItems() {
        getRegistryCenter().persist("/testJob/servers/" + new RealLocalHostService().getIp() + "/sharding", "0,1,2");
        getRegistryCenter().persist("/testJob/execution/0/failover", "host0");
        getRegistryCenter().persist("/testJob/execution/1/failover", "host1");
        assertThat(failoverService.getLocalHostTakeOffItems(), is(Arrays.asList(0, 1)));
    }
    
    @Test
    public void assertRemoveFailoverInfo() {
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        failoverService.removeFailoverInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/failover"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/completed"));
    }
}
