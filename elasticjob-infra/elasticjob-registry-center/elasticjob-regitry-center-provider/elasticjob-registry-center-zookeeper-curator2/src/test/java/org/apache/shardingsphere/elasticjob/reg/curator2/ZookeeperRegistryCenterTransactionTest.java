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

package org.apache.shardingsphere.elasticjob.reg.curator2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.curator2.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.reg.curator2.util.ZookeeperRegistryCenterTestUtil;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class ZookeeperRegistryCenterTransactionTest {
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION =
            new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), ZookeeperRegistryCenterTransactionTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        EmbedTestingServer.start();
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
    }
    
    @Before
    public void setup() {
        ZookeeperRegistryCenterTestUtil.persist(zkRegCenter);
    }
    
    @Test
    public void assertExecuteInTransactionSucceeded() throws Exception {
        List<TransactionOperation> operations = new ArrayList<>(3);
        operations.add(TransactionOperation.opCheckExists("/test"));
        operations.add(TransactionOperation.opCheckExists("/test/child"));
        operations.add(TransactionOperation.opCheckExists("/test/deep/nested"));
        operations.add(TransactionOperation.opAdd("/test/transaction", "transaction"));
        zkRegCenter.executeInTransaction(operations);
        assertThat(zkRegCenter.getDirectly("/test/transaction"), is("transaction"));
    }
    
    @Test
    public void assertExecuteInTransactionFailed() throws Exception {
        List<TransactionOperation> operations = new ArrayList<>(3);
        operations.add(TransactionOperation.opAdd("/test/shouldnotexists", ""));
        operations.add(TransactionOperation.opCheckExists("/test/notexists"));
        try {
            zkRegCenter.executeInTransaction(operations);
        } catch (KeeperException ignored) {
        }
        assertFalse(zkRegCenter.isExisted("/test/shouldnotexists"));
    }
}
