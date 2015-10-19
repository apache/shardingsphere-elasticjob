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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.test.WaitingUtils;

public final class FailoverListenerManagerTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final FailoverListenerManager failoverListenerManager = new FailoverListenerManager(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        failoverListenerManager.listenJobCrashed();
        failoverListenerManager.listenFailoverJobCrashed();
        failoverListenerManager.listenFailoverSettingsChanged();
    }
    
    @Test
    public void assertListenJobCrashedWhenNotFailover() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/running");
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenJobCrashedWhenFailoverAndItemCompleted() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/running");
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenJobCrashedWhenFailoverAndItemUncompleted() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/running");
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenFailoverJobCrashedWhenNotFailover() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/failover");
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenFailoverJobCrashedWhenFailoverAndItemCompleted() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/failover");
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenFailoverJobCrashedWhenFailoverAndItemUncompleted() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/failover", localHostService.getIp());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/execution/0/failover");
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/failover/items/0"));
    }
    
    @Test
    public void assertListenFailoverSerttingsChangedWhenFailoverIsTrue() {
        getRegistryCenter().persist("/testJob/execution/0/failover", "host0");
        getRegistryCenter().persist("/testJob/config/failover", Boolean.FALSE.toString());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().update("/testJob/config/failover", Boolean.TRUE.toString());
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/failover"));
    }
    
    @Test
    public void assertListenFailoverSerttingsChangedWhenFailoverIsFalse() {
        getRegistryCenter().persist("/testJob/execution/0/failover", "host0");
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().update("/testJob/config/failover", Boolean.FALSE.toString());
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/failover"));
    }
}
