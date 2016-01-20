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

package com.dangdang.ddframe.job.internal.monitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;

public abstract class AbstractMonitorServiceTest extends AbstractBaseJobTest {
    
    private int monitorPort;
    
    private JobScheduler jobScheduler;
    
    public AbstractMonitorServiceTest(final int monitorPort) {
        this.monitorPort = monitorPort;
    }
    
    @Before
    public void setUp() {
        JobConfiguration jobConfig = getJobConfig();
        jobConfig.setMonitorPort(monitorPort);
        jobScheduler = new JobScheduler(getRegistryCenter(), jobConfig);
        jobScheduler.init();
    }
    
    @After
    public void tearDown() {
        jobScheduler.stopJob();
        jobScheduler.shutdown();
    }
    
    protected String sendCommand(final String command, final int serverPort) throws IOException {
        try (
                Socket socket = new Socket("127.0.0.1", serverPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
            writer.write(command);
            writer.newLine();
            writer.flush();
            return reader.readLine();
        }
    }
}
