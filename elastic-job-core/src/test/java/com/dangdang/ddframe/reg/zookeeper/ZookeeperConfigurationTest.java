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

import org.junit.Test;

public final class ZookeeperConfigurationTest {
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenPortIsNegative() throws Exception {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenDataDirIsNull() throws Exception {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsNotUseNestedZookeeperWhenDataDirIsEmpty() throws Exception {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        zkConfig.setNestedDataDir("");
        assertFalse(zkConfig.isUseNestedZookeeper());
    }
    
    @Test
    public void assertIsUseNestedZookeeper() throws Exception {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        zkConfig.setNestedPort(3181);
        zkConfig.setNestedDataDir("target");
        assertTrue(zkConfig.isUseNestedZookeeper());
    }
}
