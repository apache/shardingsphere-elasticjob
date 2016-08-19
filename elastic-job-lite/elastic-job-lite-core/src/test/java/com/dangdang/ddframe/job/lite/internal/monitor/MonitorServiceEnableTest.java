/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.monitor;

import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.integrate.AbstractBaseStdJobTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class MonitorServiceEnableTest extends AbstractBaseStdJobTest {
    
    private static final int MONITOR_PORT = 9000;
    
    public MonitorServiceEnableTest() {
        super(TestSimpleJob.class, MONITOR_PORT);
    }
    
    @Test
    public void assertMonitorWithCommand() throws IOException {
        initJob();
        assertNotNull(SocketUtils.sendCommand(MonitorService.DUMP_COMMAND, MONITOR_PORT));
        assertNull(SocketUtils.sendCommand("unknown_command", MONITOR_PORT));
    }
}
