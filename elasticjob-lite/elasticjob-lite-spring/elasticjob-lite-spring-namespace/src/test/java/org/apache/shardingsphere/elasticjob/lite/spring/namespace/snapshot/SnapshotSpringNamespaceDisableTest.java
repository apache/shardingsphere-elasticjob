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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.snapshot;

import org.apache.shardingsphere.elasticjob.lite.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.test.AbstractZookeeperJUnit4SpringContextTests;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ContextConfiguration(locations = "classpath:META-INF/snapshot/snapshotDisabled.xml")
public final class SnapshotSpringNamespaceDisableTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    @Test(expected = IOException.class)
    public void assertSnapshotDisable() throws IOException {
        SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND, 9998);
    }
}
