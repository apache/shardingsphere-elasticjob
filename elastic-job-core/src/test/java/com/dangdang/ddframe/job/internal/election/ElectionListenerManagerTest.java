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

package com.dangdang.ddframe.job.internal.election;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.test.WaitingUtils;

public final class ElectionListenerManagerTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        electionListenerManager.listenLeaderElection();
    }
    
    @Test
    public void assertListenLeaderElection() {
        getRegistryCenter().persist("/testJob/leader/election/host", "host0");
        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
        WaitingUtils.waitingShortTime();
        getRegistryCenter().remove("/testJob/leader/election");
        WaitingUtils.waitingShortTime();
        assertThat(getRegistryCenter().get("/testJob/leader/election/host"), is(localHostService.getIp()));
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
    }
}
