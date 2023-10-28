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

package org.apache.shardingsphere.elasticjob.reg.zookeeper;

import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.apache.zookeeper.KeeperException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ZookeeperRegistryCenterTransactionTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(9181);
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION =
            new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterTransactionTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
    }
    
    @BeforeEach
    void setup() {
        ZookeeperRegistryCenterTestUtil.persist(zkRegCenter);
    }
    
    @Test
    void assertExecuteInTransactionSucceeded() throws Exception {
        List<TransactionOperation> operations = new ArrayList<>(3);
        operations.add(TransactionOperation.opCheckExists("/test"));
        operations.add(TransactionOperation.opCheckExists("/test/child"));
        operations.add(TransactionOperation.opCheckExists("/test/deep/nested"));
        operations.add(TransactionOperation.opAdd("/test/transaction", "transaction"));
        zkRegCenter.executeInTransaction(operations);
        assertThat(zkRegCenter.getDirectly("/test/transaction"), is("transaction"));
    }
    
    @Test
    void assertExecuteInTransactionFailed() throws Exception {
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
