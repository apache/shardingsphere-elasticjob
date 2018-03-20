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

package io.elasticjob.lite.internal.storage;

import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionBridge;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.TransactionCheckBuilder;
import org.apache.curator.framework.api.transaction.TransactionCreateBuilder;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobNodeStorageTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private JobNodeStorage jobNodeStorage = new JobNodeStorage(regCenter, "test_job");
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
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
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
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
        CuratorTransaction curatorTransaction = mock(CuratorTransaction.class);
        TransactionCheckBuilder transactionCheckBuilder = mock(TransactionCheckBuilder.class);
        CuratorTransactionBridge curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        CuratorTransactionFinal curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        when(regCenter.getRawClient()).thenReturn(client);
        when(client.inTransaction()).thenReturn(curatorTransaction);
        when(curatorTransaction.check()).thenReturn(transactionCheckBuilder);
        when(transactionCheckBuilder.forPath("/")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        TransactionCreateBuilder transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        when(curatorTransactionFinal.create()).thenReturn(transactionCreateBuilder);
        when(transactionCreateBuilder.forPath("/test_transaction")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        jobNodeStorage.executeInTransaction(new TransactionExecutionCallback() {
            
            @Override
            public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
                curatorTransactionFinal.create().forPath("/test_transaction").and();
            }
        });
        verify(regCenter).getRawClient();
        verify(client).inTransaction();
        verify(curatorTransaction).check();
        verify(transactionCheckBuilder).forPath("/");
        verify(curatorTransactionBridge, times(2)).and();
        verify(curatorTransactionFinal).create();
        verify(transactionCreateBuilder).forPath("/test_transaction");
        verify(curatorTransactionFinal).commit();
    }

    @Test(expected = RuntimeException.class)
    public void assertExecuteInTransactionFailure() throws Exception {
        CuratorFramework client = mock(CuratorFramework.class);
        CuratorTransaction curatorTransaction = mock(CuratorTransaction.class);
        TransactionCheckBuilder transactionCheckBuilder = mock(TransactionCheckBuilder.class);
        CuratorTransactionBridge curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        CuratorTransactionFinal curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        when(regCenter.getRawClient()).thenReturn(client);
        when(client.inTransaction()).thenReturn(curatorTransaction);
        when(curatorTransaction.check()).thenReturn(transactionCheckBuilder);
        when(transactionCheckBuilder.forPath("/")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        TransactionCreateBuilder transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        when(curatorTransactionFinal.create()).thenReturn(transactionCreateBuilder);
        when(transactionCreateBuilder.forPath("/test_transaction")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenThrow(new RuntimeException());
        jobNodeStorage.executeInTransaction(new TransactionExecutionCallback() {

            @Override
            public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
                curatorTransactionFinal.create().forPath("/test_transaction").and();
            }
        });
        verify(regCenter).getRawClient();
        verify(client).inTransaction();
        verify(curatorTransaction).check();
        verify(transactionCheckBuilder).forPath("/");
        verify(curatorTransactionBridge, times(2)).and();
        verify(curatorTransactionFinal).create();
        verify(transactionCreateBuilder).forPath("/test_transaction");
        verify(curatorTransactionFinal, times(0)).commit();
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
        TreeCache treeCache = mock(TreeCache.class);
        @SuppressWarnings("unchecked")
        Listenable<TreeCacheListener> listeners = mock(Listenable.class);
        TreeCacheListener listener = mock(TreeCacheListener.class);
        when(treeCache.getListenable()).thenReturn(listeners);
        when(regCenter.getRawCache("/test_job")).thenReturn(treeCache);
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
