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

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;

import static org.junit.Assert.assertNull;

public final class SnapshotServiceDisableTest extends BaseSnapshotServiceTest {
    
    public SnapshotServiceDisableTest() {
        super(new DetailedFooJob());
    }
    
    @Test(expected = IOException.class)
    public void assertMonitorWithDumpCommand() throws IOException {
        SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND, DUMP_PORT - 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertPortInvalid() {
        SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), -1);
        snapshotService.listen();
    }
    
    @Test
    @SneakyThrows
    public void assertListenException() {
        ServerSocket serverSocket = new ServerSocket(9898);
        SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), 9898);
        snapshotService.listen();
        serverSocket.close();
        Field field = snapshotService.getClass().getDeclaredField("serverSocket");
        field.setAccessible(true);
        assertNull(field.get(snapshotService));
    }
}
