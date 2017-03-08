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

package com.dangdang.ddframe.job.example;

import org.apache.curator.test.TestingServer;

import java.io.File;
import java.io.IOException;

/**
 * 内存版的内嵌Zookeeper.
 * 
 * <p>
 *     仅用于运行Elastic-Job的例子时无需额外启动Zookeeper. 如有必要, 请使用本地环境可用的Zookeeper代替.
 * </p>
 */
public final class EmbedZookeeperServer {
    
    private static TestingServer testingServer;
    
    /**
     * 内存版的内嵌Zookeeper.
     * 
     * @param port Zookeeper的通信端口号
     */
    public static void start(final int port) {
        try {
            testingServer = new TestingServer(port, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            ex.printStackTrace();
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000L);
                        testingServer.close();
                    } catch (final InterruptedException | IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }
}
