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

package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class SnapshotServiceEnableTest extends BaseSnapshotServiceTest {
    
    public SnapshotServiceEnableTest() {
        super(new DetailedFooJob());
    }
    
    @Before
    public void listenMonitor() {
        getSnapshotService().listen();
    }
    
    @After
    public void closeMonitor() {
        getSnapshotService().close();
    }
    
    @Test
    public void assertMonitorWithCommand() throws IOException {
        assertNotNull(SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND + getJobName(), DUMP_PORT));
        assertNull(SocketUtils.sendCommand("unknown_command", DUMP_PORT));
    }
}
