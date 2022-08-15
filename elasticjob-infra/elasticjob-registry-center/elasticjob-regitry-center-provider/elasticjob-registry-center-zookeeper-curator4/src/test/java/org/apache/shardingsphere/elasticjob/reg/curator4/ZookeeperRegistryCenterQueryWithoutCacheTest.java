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

package org.apache.shardingsphere.elasticjob.reg.curator4;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.elasticjob.reg.curator4.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.reg.curator4.util.ZookeeperRegistryCenterTestUtil;

import java.util.Arrays;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public final class ZookeeperRegistryCenterQueryWithoutCacheTest {
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = 
            new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), ZookeeperRegistryCenterQueryWithoutCacheTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        zkRegCenter.init();
        ZookeeperRegistryCenterTestUtil.persist(zkRegCenter);
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
    public void assertGetNumChildren() {
        assertThat(zkRegCenter.getNumChildren("/test"), is(2));
        assertThat(zkRegCenter.getNumChildren("/test/deep"), is(1));
        assertThat(zkRegCenter.getNumChildren("/test/child"), is(0));
        assertThat(zkRegCenter.getNumChildren("/test/notExisted"), is(0));
    }
    
    @Test
    public void assertIsExisted() {
        assertTrue(zkRegCenter.isExisted("/test"));
        assertTrue(zkRegCenter.isExisted("/test/deep/nested"));
        assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test
    public void assertGetRegistryCenterTime() {
        long regCenterTime = zkRegCenter.getRegistryCenterTime("/_systemTime/current");
        assertTrue(regCenterTime <= System.currentTimeMillis());
        long updatedRegCenterTime = zkRegCenter.getRegistryCenterTime("/_systemTime/current");
        assertTrue(regCenterTime <= updatedRegCenterTime);
    }
    
    @Test
    public void assertGetWithoutNode() {
        assertNull(zkRegCenter.get("/notExisted"));
    }
}
