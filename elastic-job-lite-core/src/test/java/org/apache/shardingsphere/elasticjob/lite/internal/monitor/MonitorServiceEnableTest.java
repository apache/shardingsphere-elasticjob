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

package org.apache.shardingsphere.elasticjob.lite.internal.monitor;

import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.integrate.AbstractBaseStdJobTest;
import org.apache.shardingsphere.elasticjob.lite.integrate.fixture.simple.FooSimpleElasticJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class MonitorServiceEnableTest extends AbstractBaseStdJobTest {
    
    public MonitorServiceEnableTest() {
        super(new FooSimpleElasticJob());
    }
    
    @Before
    public void setUp() {
        super.setUp();
        getMonitorService().listen();
    }
    
    @After
    public void tearDown() {
        super.tearDown();
        getMonitorService().close();
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final ElasticJob elasticJob, final String jobName) {
        return JobConfiguration.newBuilder(jobName, JobType.SIMPLE, 3).cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").jobErrorHandlerType("IGNORE").overwrite(true).build();
    }
    
    @Test
    public void assertMonitorWithCommand() throws IOException {
        scheduleJob();
        assertNotNull(SocketUtils.sendCommand(MonitorService.DUMP_COMMAND + getJobName(), MONITOR_PORT));
        assertNull(SocketUtils.sendCommand("unknown_command", MONITOR_PORT));
    }
}
