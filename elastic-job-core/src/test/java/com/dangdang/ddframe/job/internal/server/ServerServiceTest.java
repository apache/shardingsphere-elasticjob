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

package com.dangdang.ddframe.job.internal.server;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;

public final class ServerServiceTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final ServerService serverService = new ServerService(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        getJobConfig().setOverwrite(true);
    }
    
    @Test
    public void assertPersistServerOnline() {
        serverService.persistServerOnline();
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/hostName"), is(localHostService.getHostName()));
        assertFalse(getRegistryCenter().isExisted("/testJob/servers/" + localHostService.getIp() + "/disabled"));
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
    }
    
    @Test
    public void assertPersistServerOnlineWhenOverwrite() {
        ServerService serverServiceForOverwrite = new ServerService(getRegistryCenter(), createJobConfigurationForOverwrite());
        serverServiceForOverwrite.persistServerOnline();
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/hostName"), is(localHostService.getHostName()));
        assertTrue(getRegistryCenter().isExisted("/testJob/servers/" + localHostService.getIp() + "/disabled"));
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
    }
    
    private JobConfiguration createJobConfigurationForOverwrite() {
        JobConfiguration result = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
        result.setDisabled(true);
        result.setOverwrite(true);
        return result;
    }
    
    @Test
    public void assertPersistServerOnlineWhenNotOverwrite() {
        ServerService serverServiceForNotOverwrite = new ServerService(getRegistryCenter(), createJobConfigurationForNotOverwrite());
        serverServiceForNotOverwrite.persistServerOnline();
    }
    
    private JobConfiguration createJobConfigurationForNotOverwrite() {
        JobConfiguration result = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
        result.setDisabled(true);
        return result;
    }
    
    @Test
    public void assertClearJobStopedStatus() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/stoped", "");
        serverService.clearJobStopedStatus();
        assertFalse(getRegistryCenter().isExisted("/testJob/servers/" + localHostService.getIp() + "/stoped"));
    }
    
    @Test
    public void assertIsJobStopedManually() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/stoped", "");
        assertTrue(serverService.isJobStopedManually());
    }
    
    @Test
    public void assertIsNotJobStopedManually() {
        assertFalse(serverService.isJobStopedManually());
    }
    
    @Test
    public void assertUpdateServerStatus() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        serverService.updateServerStatus(ServerStatus.RUNNING);
        assertThat(getRegistryCenter().getDirectly("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.RUNNING.name()));
    }
    
    @Test
    public void assertGetAllServers() {
        getRegistryCenter().persist("/testJob/servers/host0/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host1/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host1/disabled", "");
        getRegistryCenter().persist("/testJob/servers/host2/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host3", "");
        assertThat(serverService.getAllServers(), is(Arrays.asList("host0", "host1", "host2", "host3")));
    }
    
    @Test
    public void assertGetAvailableServers() {
        getRegistryCenter().persist("/testJob/servers/host4/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host0/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host1/disabled", "");
        getRegistryCenter().persist("/testJob/servers/host1/status", ServerStatus.READY.name());
        getRegistryCenter().persist("/testJob/servers/host2", "");
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host4")));
    }
    
    @Test
    public void assertIsServerReadyWhenServerDisabled() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/disabled", "");
        assertFalse(serverService.isServerReady());
    }
    
    @Test
    public void assertIsServerReadyWhenServerStoped() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/stoped", "");
        assertFalse(serverService.isServerReady());
    }
    
    @Test
    public void assertIsServerReadyWhenServerCrashed() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp(), "");
        assertFalse(serverService.isServerReady());
    }
    
    @Test
    public void assertIsServerReadyWhenServerRunning() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.RUNNING.name());
        assertFalse(serverService.isServerReady());
    }
    
    @Test
    public void assertIsServerReadyWhenServerReady() {
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        assertTrue(serverService.isServerReady());
    }
    
    @Test
    public void assertPersistProcessSuccessCount() {
        serverService.persistProcessSuccessCount(100);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processSuccessCount"), is("100"));
    }
    
    @Test
    public void assertPersistProcessFailureCount() {
        serverService.persistProcessFailureCount(10);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processFailureCount"), is("10"));
    }
}
