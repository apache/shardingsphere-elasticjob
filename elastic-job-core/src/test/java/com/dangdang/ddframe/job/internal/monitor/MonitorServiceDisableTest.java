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

import java.io.IOException;

import org.junit.Test;

import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.integrate.AbstractBaseStdJobTest;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;

public final class MonitorServiceDisableTest extends AbstractBaseStdJobTest {
    
    public MonitorServiceDisableTest() {
        super(TestJob.class, -1);
    }
    
    @Test(expected = IOException.class)
    public void assertMonitorWithDumpCommand() throws IOException {
        SocketUtils.sendCommand(MonitorService.DUMP_COMMAND, 9000);
    }
}
