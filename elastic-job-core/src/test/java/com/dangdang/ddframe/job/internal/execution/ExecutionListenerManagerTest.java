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

package com.dangdang.ddframe.job.internal.execution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.test.WaitingUtils;

public final class ExecutionListenerManagerTest extends AbstractBaseJobTest {
    
    private final ExecutionListenerManager executionListenerManager = new ExecutionListenerManager(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        executionListenerManager.listenMonitorExecutionChanged();
    }
    
    @Test
    public void assertListenMonitorExecutionChangedWhenCreate() {
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/running"));
    }
    
    @Test
    public void assertListenMonitorExecutionChangedWhenUpdateTrue() {
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        WaitingUtils.waitingShortTime();
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/running"));
    }
    
    @Test
    public void assertListenMonitorExecutionChangedWhenUpdateFalse() {
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        WaitingUtils.waitingShortTime();
        getRegistryCenter().update("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        WaitingUtils.waitingShortTime();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution"));
    }
}
