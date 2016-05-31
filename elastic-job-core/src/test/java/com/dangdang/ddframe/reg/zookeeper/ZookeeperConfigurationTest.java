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

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ZookeeperConfigurationTest {
    
    @Test
    public void assertNewZookeeperConfigurationForServerListsAndNamespace() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "myNamespace");
        assertThat(zkConfig.getServerLists(), is("localhost:2181"));
        assertThat(zkConfig.getNamespace(), is("myNamespace"));
        assertThat(zkConfig.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(zkConfig.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(zkConfig.getMaxRetries(), is(3));
    }
    
    @Test
    public void assertNewZookeeperConfigurationForMaxConstructor() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "myNamespace", 2000, 6000, 6);
        assertThat(zkConfig.getServerLists(), is("localhost:2181"));
        assertThat(zkConfig.getNamespace(), is("myNamespace"));
        assertThat(zkConfig.getBaseSleepTimeMilliseconds(), is(2000));
        assertThat(zkConfig.getMaxSleepTimeMilliseconds(), is(6000));
        assertThat(zkConfig.getMaxRetries(), is(6));
    }
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenPortIsNegative() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenDataDirIsNull() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenDataDirIsEmpty() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        zkConfig.setNestedDataDir("");
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsUseNestedZookeeper() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        zkConfig.setNestedDataDir("target");
        assertTrue(zkConfig.isUseNestedZookeeper());
    }
}
