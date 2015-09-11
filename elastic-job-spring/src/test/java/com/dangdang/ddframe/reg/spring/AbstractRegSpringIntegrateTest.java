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

package com.dangdang.ddframe.reg.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;

import org.junit.Test;

import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.test.AbstractZookeeperJUnit4SpringContextTests;

public abstract class AbstractRegSpringIntegrateTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    @Resource(name = "regCenter1")
    private ZookeeperRegistryCenter zookeeperRegistryCenter1;
    
    @Resource(name = "regCenter2")
    private ZookeeperRegistryCenter zookeeperRegistryCenter2;
    
    @Test
    public void assertSpringSupport() {
        assertThat(zookeeperRegistryCenter1.get("/test"), is("test"));
        assertThat(zookeeperRegistryCenter1.get("/test/deep/nested"), is("deepNested"));
        assertThat(zookeeperRegistryCenter2.get("/test"), is("test_overwrite"));
        assertThat(zookeeperRegistryCenter2.get("/test/deep/nested"), is("deepNested_overwrite"));
        assertThat(zookeeperRegistryCenter2.get("/new"), is("new"));
    }
}
