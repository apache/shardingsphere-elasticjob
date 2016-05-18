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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dangdang.ddframe.reg.AbstractNestedZookeeperBaseTest;

public final class ZookeeperRegistryCenterQueryWithoutCacheTest extends AbstractNestedZookeeperBaseTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZK_CONNECTION_STRING, ZookeeperRegistryCenterQueryWithoutCacheTest.class.getName(), 1000, 3000, 3);
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted(PORT, TEST_TEMP_DIRECTORY);
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
        zkRegCenter.init();
        zkRegCenter.addCacheData("/other");
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertGetFromServer() {
        assertThat(zkRegCenter.get("/test"), is("test"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        assertThat(zkRegCenter.getChildrenKeys("/test"), is(Arrays.asList("deep", "child")));
        assertThat(zkRegCenter.getChildrenKeys("/test/deep"), is(Collections.singletonList("nested")));
        assertThat(zkRegCenter.getChildrenKeys("/test/child"), is(Collections.<String>emptyList()));
        assertThat(zkRegCenter.getChildrenKeys("/test/notExisted"), is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsExisted() {
        assertTrue(zkRegCenter.isExisted("/test"));
        assertTrue(zkRegCenter.isExisted("/test/deep/nested"));
        assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test
    public void assertGetRegistryCenterTime() {
        assertTrue(zkRegCenter.getRegistryCenterTime("/_systemTime/current") <= System.currentTimeMillis());
    }
}
