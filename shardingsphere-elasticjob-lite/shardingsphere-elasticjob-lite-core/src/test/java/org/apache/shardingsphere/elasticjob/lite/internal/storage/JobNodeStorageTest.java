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

package org.apache.shardingsphere.elasticjob.lite.internal.storage;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionCheckBuilder;
import org.apache.curator.framework.api.transaction.TransactionCreateBuilder;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobNodeStorageTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private JobNodeStorage jobNodeStorage;
    
    @Before
    public void setUp() {
        jobNodeStorage = new JobNodeStorage(regCenter, "test_job");
        ReflectionUtils.setFieldValue(jobNodeStorage, "regCenter", regCenter);
    }
    
    @Test
    public void assertIsJobNodeExisted() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        assertTrue(jobNodeStorage.isJobNodeExisted("config"));
        verify(regCenter).isExisted("/test_job/config");
    }
    
    @Test
    public void assertGetJobNodeData() {
        when(regCenter.get("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeData("config/cron"), is("0/1 * * * * ?"));
        verify(regCenter).get("/test_job/config/cron");
    }
    
    @Test
    public void assertGetJobNodeDataDirectly() {
        when(regCenter.getDirectly("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeDataDirectly("config/cron"), is("0/1 * * * * ?"));
        verify(regCenter).getDirectly("/test_job/config/cron");
    }
    
    @Test
    public void assertGetJobNodeChildrenKeys() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("host0", "host1"));
        assertThat(jobNodeStorage.getJobNodeChildrenKeys("servers"), is(Arrays.asList("host0", "host1")));
        verify(regCenter).getChildrenKeys("/test_job/servers");
    }
    
    @Test
    public void assertCreateJobNodeIfNeeded() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter).persist("/test_job/config", "");
    }
    
    @Test
    public void assertCreateJobNodeIfRootJobNodeIsNotExist() {
        when(regCenter.isExisted("/test_job")).thenReturn(false);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter, times(0)).isExisted("/test_job/config");
        verify(regCenter, times(0)).persist("/test_job/config", "");
    }
    
    @Test
    public void assertCreateJobNodeIfNotNeeded() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter, times(0)).persist("/test_job/config", "");
    }
    
    @Test
    public void assertRemoveJobNodeIfNeeded() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter).remove("/test_job/config");
    }
    
    @Test
    public void assertRemoveJobNodeIfNotNeeded() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter, times(0)).remove("/test_job/config");
    }
    
    @Test
    public void assertFillJobNode() {
        jobNodeStorage.fillJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persist("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertFillEphemeralJobNode() {
        jobNodeStorage.fillEphemeralJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persistEphemeral("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertUpdateJobNode() {
        jobNodeStorage.updateJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).update("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertReplaceJobNode() {
        jobNodeStorage.replaceJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persist("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertExecuteInTransactionSuccess() throws Exception {
        CuratorFramework client = mock(CuratorFramework.class);
        when(regCenter.getRawClient()).thenReturn(client);
        TransactionOp transactionOp = mockTransactionOp(client);
        CuratorOp checkOp = mockCheckOp(transactionOp);
        CuratorOp createOp = mockCreateOp(transactionOp);
        CuratorMultiTransaction transaction = mockTransaction(client);
        jobNodeStorage.executeInTransaction(input -> Collections.singletonList(input.create().forPath("/test_transaction")));
        verify(transaction).forOperations(Arrays.asList(checkOp, createOp));
    }
    
    private TransactionOp mockTransactionOp(final CuratorFramework client) {
        TransactionOp result = mock(TransactionOp.class);
        when(client.transactionOp()).thenReturn(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private CuratorOp mockCheckOp(final TransactionOp transactionOperation) throws Exception {
        TransactionCheckBuilder transactionCheckBuilder = mock(TransactionCheckBuilder.class);
        when(transactionOperation.check()).thenReturn(transactionCheckBuilder);
        CuratorOp result = mock(CuratorOp.class);
        when(transactionCheckBuilder.forPath("/")).thenReturn(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private CuratorOp mockCreateOp(final TransactionOp transactionOperation) throws Exception {
        TransactionCreateBuilder transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        when(transactionOperation.create()).thenReturn(transactionCreateBuilder);
        CuratorOp result = mock(CuratorOp.class);
        when(transactionCreateBuilder.forPath("/test_transaction")).thenReturn(result);
        return result;
    }
    
    private CuratorMultiTransaction mockTransaction(final CuratorFramework client) {
        CuratorMultiTransaction result = mock(CuratorMultiTransaction.class);
        when(client.transaction()).thenReturn(result);
        return result;
    }
    
    @Test(expected = RuntimeException.class)
    public void assertExecuteInTransactionFailure() throws Exception {
        CuratorFramework client = mock(CuratorFramework.class);
        when(regCenter.getRawClient()).thenReturn(client);
        TransactionOp transactionOp = mockTransactionOp(client);
        CuratorOp checkOp = mockCheckOp(transactionOp);
        CuratorOp createFailedOp = mockCreateFailedOp(transactionOp);
        CuratorMultiTransaction transaction = mockTransaction(client);
        try {
            jobNodeStorage.executeInTransaction(input -> Collections.singletonList(input.create().forPath("/test_transaction")));
        } finally {
            verify(transaction, times(0)).forOperations(Arrays.asList(checkOp, createFailedOp));
        }
    }
    
    @SuppressWarnings("unchecked")
    private CuratorOp mockCreateFailedOp(final TransactionOp transactionOperation) throws Exception {
        TransactionCreateBuilder transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        when(transactionOperation.create()).thenReturn(transactionCreateBuilder);
        CuratorOp result = mock(CuratorOp.class);
        when(transactionCreateBuilder.forPath("/test_transaction")).thenThrow(new RuntimeException());
        return result;
    }
    
    @Test
    public void assertAddConnectionStateListener() {
        CuratorFramework client = mock(CuratorFramework.class);
        @SuppressWarnings("unchecked")
        Listenable<ConnectionStateListener> listeners = mock(Listenable.class);
        ConnectionStateListener listener = mock(ConnectionStateListener.class);
        when(client.getConnectionStateListenable()).thenReturn(listeners);
        when(regCenter.getRawClient()).thenReturn(client);
        jobNodeStorage.addConnectionStateListener(listener);
        verify(listeners).addListener(listener);
    }
    
    @Test
    public void assertAddDataListener() {
        CuratorCache cache = mock(CuratorCache.class);
        @SuppressWarnings("unchecked")
        Listenable<CuratorCacheListener> listeners = mock(Listenable.class);
        CuratorCacheListener listener = mock(CuratorCacheListener.class);
        when(cache.listenable()).thenReturn(listeners);
        when(regCenter.getRawCache("/test_job")).thenReturn(cache);
        jobNodeStorage.addDataListener(listener);
        verify(listeners).addListener(listener);
    }
    
    @Test
    public void assertGetRegistryCenterTime() {
        when(regCenter.getRegistryCenterTime("/test_job/systemTime/current")).thenReturn(0L);
        assertThat(jobNodeStorage.getRegistryCenterTime(), is(0L));
        verify(regCenter).getRegistryCenterTime("/test_job/systemTime/current");
    }
}
