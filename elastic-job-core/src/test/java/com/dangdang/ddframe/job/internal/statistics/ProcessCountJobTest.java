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

package com.dangdang.ddframe.job.internal.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;

public final class ProcessCountJobTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final ProcessCountJob processCountJob = new ProcessCountJob(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        ProcessCountStatistics.incrementProcessSuccessCount("testJob");
        ProcessCountStatistics.incrementProcessSuccessCount("otherTestJob");
        ProcessCountStatistics.incrementProcessFailureCount("testJob");
        ProcessCountStatistics.incrementProcessFailureCount("otherTestJob");
    }
    
    @Test
    public void assertRun() throws JobExecutionException {
        processCountJob.run();
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processSuccessCount"), is("1"));
        assertFalse(getRegistryCenter().isExisted("/otherTestJob/servers/" + localHostService.getIp() + "/processSuccessCount"));
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processFailureCount"), is("1"));
        assertFalse(getRegistryCenter().isExisted("/otherTestJob/servers/" + localHostService.getIp() + "/processFailureCount"));
        processCountJob.run();
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processSuccessCount"), is("0"));
        assertFalse(getRegistryCenter().isExisted("/otherTestJob/servers/" + localHostService.getIp() + "/processSuccessCount"));
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/processFailureCount"), is("0"));
        assertFalse(getRegistryCenter().isExisted("/otherTestJob/servers/" + localHostService.getIp() + "/processFailureCount"));
    }
}
