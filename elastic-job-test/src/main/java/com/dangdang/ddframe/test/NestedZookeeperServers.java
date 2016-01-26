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

package com.dangdang.ddframe.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.test.TestingServer;

/**
 * 启动用于测试的内嵌Zookeeper服务.
 * 
 * <p>
 * 可以根据不同的端口号启动多个Zookeeper服务.
 * 但每个相同的端口号共用一个服务实例.
 * </p>
 * 
 * <p>
 * 整个测试结束后, 随着JVM的关闭而关闭内嵌Zookeeper服务器.
 * 之所以不调用close方法关闭Zookeeper容器并清理资源, 原因是Curator连接时会不停的扫描Zookeeper, 如果Zookeeper先于Curator关闭, Curator会不停的重连Zookeeper容器, 导致测试用例不能继续进行.
 * 所以只能采用这种方式关闭, 目前已知的问题是: 测试的文件夹test_zk_data不能在测试结束后自动删除.
 * </p>
 * 
 * @author zhangliang
 */
public final class NestedZookeeperServers {
    
    /**
     * 内嵌Zookeeper的连接地址.
     */
    public static final String ZK_CONNECTION_STRING = String.format("localhost:%s", NestedZookeeperServers.DEFAULT_PORT);
    
    private static final int DEFAULT_PORT = 3181;
    
    private static final String TEST_TEMP_DIRECTORY = String.format("target/test_zk_data/%s/", System.nanoTime());
    
    private static NestedZookeeperServers instance = new NestedZookeeperServers();
    
    private static ConcurrentMap<Integer, TestingServer> testingServers = new ConcurrentHashMap<>();
    
    private NestedZookeeperServers() {
    }
    
    /**
     * 获取单例实例.
     * 
     * @return 单例实例
     */
    public static NestedZookeeperServers getInstance() {
        return instance;
    }
    
    /**
     * 启动内嵌的端口号为3181的Zookeeper服务.
     * 
     * <p>
     * 如果该端口号的Zookeeper服务未启动, 则启动服务.
     * 如果该端口号的Zookeeper服务已启动, 则不做任何操作.
     * </p>
     */
    public void startServerIfNotStarted() {
        startServerIfNotStarted(DEFAULT_PORT);
    }
    
    private synchronized void startServerIfNotStarted(final int port) {
        if (!testingServers.containsKey(port)) {
            TestingServer testingServer;
            try {
                testingServer = new TestingServer(port, new File(TEST_TEMP_DIRECTORY + port));
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            // CHECKSTYLE:ON
                throw new TestEnvironmentException(ex);
            }
            testingServers.putIfAbsent(port, testingServer);
        }
    }
    
    /**
     * 关闭内嵌的端口号为3181的Zookeeper服务.
     */
    public void closeServer() {
        try {
            testingServers.get(DEFAULT_PORT).close();
        } catch (final IOException ex) {
            throw new TestEnvironmentException(ex);
        }
    }
}
