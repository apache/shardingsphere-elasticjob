/*
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

package com.dangdang.ddframe.reg.zookeeper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.test.TestingServer;

import com.dangdang.ddframe.reg.exception.RegExceptionHandler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 内嵌的Zookeeper服务器.
 * 
 * <p>
 * 可以根据不同的端口号启动多个Zookeeper服务.
 * 但每个相同的端口号共用一个服务实例.
 * </p>
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NestedZookeeperServers {
    
    private static NestedZookeeperServers instance = new NestedZookeeperServers();
    
    private static ConcurrentMap<Integer, TestingServer> nestedServers = new ConcurrentHashMap<>();
    
    /**
     * 获取单例实例.
     * 
     * @return 单例实例
     */
    public static NestedZookeeperServers getInstance() {
        return instance;
    }
    
    /**
     * 启动内嵌的Zookeeper服务.
     * 
     * @param port 端口号
     * 
     * <p>
     * 如果该端口号的Zookeeper服务未启动, 则启动服务.
     * 如果该端口号的Zookeeper服务已启动, 则不做任何操作.
     * </p>
     */
    public synchronized void startServerIfNotStarted(final int port, final String dataDir) {
        if (!nestedServers.containsKey(port)) {
            TestingServer testingServer = null;
            try {
                testingServer = new TestingServer(port, new File(dataDir));
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            // CHECKSTYLE:ON
                RegExceptionHandler.handleException(ex);
            }
            nestedServers.putIfAbsent(port, testingServer);
        }
    }
    
    /**
     * 关闭内嵌的Zookeeper服务.
     * 
     * @param port 端口号
     */
    public void closeServer(final int port) {
        TestingServer nestedServer = nestedServers.get(port);
        if (null == nestedServer) {
            return;
        }
        try {
            nestedServer.close();
            nestedServers.remove(port);
        } catch (final IOException ex) {
            RegExceptionHandler.handleException(ex);
        }
    }
}
