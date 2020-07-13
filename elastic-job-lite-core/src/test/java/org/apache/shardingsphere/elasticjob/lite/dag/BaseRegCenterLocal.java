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

package org.apache.shardingsphere.elasticjob.lite.dag;

import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.TimeUnit;

/**
 * Use local zk.
 **/
public class BaseRegCenterLocal {
    private static final String NAME_SPACE = "testb";

    private static final ZookeeperConfiguration ZOOKEEPERCONFIGURATION = new ZookeeperConfiguration("localhost:2181", NAME_SPACE);

    //CHECKSTYLE:OFF
    public static ZookeeperRegistryCenter zkRegCenter;
    //CHECKSTYLE:ON

    @BeforeClass
    public static void setUp() throws InterruptedException {
        //ZOOKEEPER_CONFIGURATION.setDigest("digest:password");
        ZOOKEEPERCONFIGURATION.setSessionTimeoutMilliseconds(5000);
        ZOOKEEPERCONFIGURATION.setConnectionTimeoutMilliseconds(5000);
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPERCONFIGURATION);
        zkRegCenter.init();

        TimeUnit.SECONDS.sleep(2);
        System.out.println("SetUp: init zk ");

    }

    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
}
