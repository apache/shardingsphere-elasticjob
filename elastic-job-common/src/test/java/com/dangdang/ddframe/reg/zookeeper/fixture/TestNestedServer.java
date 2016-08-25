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

package com.dangdang.ddframe.reg.zookeeper.fixture;

import com.dangdang.ddframe.reg.zookeeper.NestedZookeeperServers;
import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestNestedServer {
    
    private static final int PORT = 3181;
    
    public static String getConnectionString() {
        return Joiner.on(":").join("localhost", PORT);
    }
    
    public static void start() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted(PORT, String.format("target/test_zk_data/%s/", System.nanoTime()));
    }
    
    public static void close() {
        NestedZookeeperServers.getInstance().closeServer(TestNestedServer.PORT);
    }
}
