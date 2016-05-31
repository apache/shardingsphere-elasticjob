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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dangdang.ddframe.reg.AbstractNestedZookeeperBaseTest;

public final class ZookeeperRegistryCenterQueryWithCacheTest extends AbstractNestedZookeeperBaseTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZK_CONNECTION_STRING, ZookeeperRegistryCenterQueryWithCacheTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted(PORT, TEST_TEMP_DIRECTORY);
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        zkRegCenter.addCacheData("/test");
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertGetWithoutValue() {
        assertNull(zkRegCenter.get("/test/null"));
    }
    
    @Test
    public void assertGetFromCache() {
        assertThat(zkRegCenter.get("/test"), is("test"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
    }
}
