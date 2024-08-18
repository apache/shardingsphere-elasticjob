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

package org.apache.shardingsphere.elasticjob.test.e2e.snapshot;

import org.apache.shardingsphere.elasticjob.kernel.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.test.e2e.raw.fixture.job.E2EFixtureJobImpl;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotServiceDisableE2ETest extends BaseSnapshotServiceE2ETest {
    
    SnapshotServiceDisableE2ETest() {
        super(new E2EFixtureJobImpl());
    }
    
    @Test
    void assertMonitorWithDumpCommand() {
        assertThrows(IOException.class, () -> SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND, DUMP_PORT - 1));
    }
    
    @Test
    void assertPortInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new SnapshotService(getRegCenter(), -1).listen());
    }
    
    @Test
    void assertListenException() throws IOException {
        ServerSocket serverSocket = new ServerSocket(9898);
        SnapshotService snapshotService = new SnapshotService(getRegCenter(), 9898);
        snapshotService.listen();
        serverSocket.close();
        assertNull(ReflectionUtils.getFieldValue(snapshotService, "serverSocket"));
    }
}
