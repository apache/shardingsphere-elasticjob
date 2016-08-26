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

package com.dangdang.ddframe.reg.zookeeper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ZookeeperRegistryCenterNestedTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:3181", ZookeeperRegistryCenterNestedTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        zkConfig.setNestedPort(3181);
        zkConfig.setNestedDataDir(String.format("target/test_zk_data/%s/", System.nanoTime()));
        zkRegCenter.init();
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertInit() {
        assertThat(zkRegCenter.get("/test"), is("test"));
    }
}
