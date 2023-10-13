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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class SnapshotServiceEnableTest extends BaseSnapshotServiceTest {
    
    public SnapshotServiceEnableTest() {
        super(new DetailedFooJob());
    }
    
    @BeforeEach
    public void listenMonitor() {
        getSnapshotService().listen();
    }
    
    @AfterEach
    public void closeMonitor() {
        getSnapshotService().close();
    }
    
    @Test
    public void assertMonitorWithCommand() throws IOException {
        assertNotNull(SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND + getJobName(), DUMP_PORT));
        assertEquals(SocketUtils.sendCommand("unknown_command", DUMP_PORT), "");
    }
    
    @Test
    public void assertDumpJobDirectly() {
        assertNotNull(getSnapshotService().dumpJobDirectly(getJobName()));
    }
    
    @Test
    public void assertDumpJob() throws IOException {
        assertNotNull(SnapshotService.dumpJob("127.0.0.1", DUMP_PORT, getJobName()));
    }
}
