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

package org.apache.shardingsphere.elasticjob.cloud.reg.zookeeper;

import org.apache.shardingsphere.elasticjob.cloud.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.cloud.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;

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
        Assert.assertThat(zkRegCenter.get("/test"), is("test"));
        Assert.assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        Assert.assertThat(zkRegCenter.getChildrenKeys("/test"), is(Arrays.asList("deep", "child")));
        Assert.assertThat(zkRegCenter.getChildrenKeys("/test/deep"), is(Collections.singletonList("nested")));
        Assert.assertThat(zkRegCenter.getChildrenKeys("/test/child"), is(Collections.<String>emptyList()));
        Assert.assertThat(zkRegCenter.getChildrenKeys("/test/notExisted"), is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertGetNumChildren() {
        Assert.assertThat(zkRegCenter.getNumChildren("/test"), is(2));
        Assert.assertThat(zkRegCenter.getNumChildren("/test/deep"), is(1));
        Assert.assertThat(zkRegCenter.getNumChildren("/test/child"), is(0));
        Assert.assertThat(zkRegCenter.getNumChildren("/test/notExisted"), is(0));
    }
    
    @Test
    public void assertIsExisted() {
        Assert.assertTrue(zkRegCenter.isExisted("/test"));
        Assert.assertTrue(zkRegCenter.isExisted("/test/deep/nested"));
        Assert.assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test
    public void assertGetRegistryCenterTime() {
        long regCenterTime = zkRegCenter.getRegistryCenterTime("/_systemTime/current");
        Assert.assertTrue(regCenterTime <= System.currentTimeMillis());
        long updatedRegCenterTime = zkRegCenter.getRegistryCenterTime("/_systemTime/current");
        System.out.println(regCenterTime + "," + updatedRegCenterTime);
        Assert.assertTrue(regCenterTime < updatedRegCenterTime);
    }

    @Test
    public void assertGetWithoutNode() {
        Assert.assertNull(zkRegCenter.get("/notExisted"));
    }
}
