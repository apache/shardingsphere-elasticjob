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

package com.dangdang.ddframe.reg.zookeeper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

public final class NestedZookeeperServersTest {
    
    private NestedZookeeperServers nestedZookeeperServers = NestedZookeeperServers.getInstance();
    
    @Test
    public void assertStartServerIfNotStarted() throws NoSuchFieldException, SecurityException {
        nestedZookeeperServers.startServerIfNotStarted(5555, String.format("target/test_zk_data/5555/%s/", System.nanoTime()));
        nestedZookeeperServers.startServerIfNotStarted(5555, String.format("target/test_zk_data/5555/%s/", System.nanoTime()));
        assertTrue(getNestedServers().containsKey(5555));
    }
    
    @Test
    public void assertCloseServerIfNotStarted() throws NoSuchFieldException, SecurityException {
        nestedZookeeperServers.closeServer(6666);
        assertFalse(getNestedServers().containsKey(6666));
    }
    
    @Test
    public void assertCloseServerIfStarted() throws NoSuchFieldException, SecurityException {
        nestedZookeeperServers.startServerIfNotStarted(7777, String.format("target/test_zk_data/7777/%s/", System.nanoTime()));
        nestedZookeeperServers.closeServer(7777);
        assertFalse(getNestedServers().containsKey(7777));
    }
    
    @SuppressWarnings("unchecked")
    private Map<Integer, TestingServer> getNestedServers() throws NoSuchFieldException {
        return (Map<Integer, TestingServer>) ReflectionUtils.getFieldValue(nestedZookeeperServers, NestedZookeeperServers.class.getDeclaredField("nestedServers"));
    }
}
