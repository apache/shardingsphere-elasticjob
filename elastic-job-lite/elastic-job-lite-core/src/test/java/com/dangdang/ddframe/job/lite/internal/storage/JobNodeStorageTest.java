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

package com.dangdang.ddframe.job.lite.internal.storage;

import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.util.JobConfigurationUtil;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
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
    private CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    private final LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration(false);
    
    private JobNodeStorage jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, liteJobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobNodeStorage, "coordinatorRegistryCenter", coordinatorRegistryCenter);
    }
    
    @Before
    public void reset() {
        JobConfigurationUtil.setFieldValue(liteJobConfig, "overwrite", false);
    }
    
    @Test
    public void assertIsJobNodeExisted() {
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(true);
        assertTrue(jobNodeStorage.isJobNodeExisted("config"));
        verify(coordinatorRegistryCenter).isExisted("/test_job/config");
    }
    
    @Test
    public void assertGetJobNodeData() {
        when(coordinatorRegistryCenter.get("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeData("config/cron"), is("0/1 * * * * ?"));
        verify(coordinatorRegistryCenter).get("/test_job/config/cron");
    }
    
    @Test
    public void assertGetJobNodeDataDirectly() {
        when(coordinatorRegistryCenter.getDirectly("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeDataDirectly("config/cron"), is("0/1 * * * * ?"));
        verify(coordinatorRegistryCenter).getDirectly("/test_job/config/cron");
    }
    
    @Test
    public void assertGetJobNodeChildrenKeys() {
        when(coordinatorRegistryCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("host0", "host1"));
        assertThat(jobNodeStorage.getJobNodeChildrenKeys("servers"), is(Arrays.asList("host0", "host1")));
        verify(coordinatorRegistryCenter).getChildrenKeys("/test_job/servers");
    }
    
    @Test
    public void assertCreateJobNodeIfNeeded() {
        when(coordinatorRegistryCenter.isExisted("/test_job")).thenReturn(true);
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(coordinatorRegistryCenter).isExisted("/test_job");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config");
        verify(coordinatorRegistryCenter).persist("/test_job/config", "");
    }
    
    @Test
    public void assertCreateJobNodeIfRootJobNodeIsNotExist() {
        when(coordinatorRegistryCenter.isExisted("/test_job")).thenReturn(false);
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(coordinatorRegistryCenter).isExisted("/test_job");
        verify(coordinatorRegistryCenter, times(0)).isExisted("/test_job/config");
        verify(coordinatorRegistryCenter, times(0)).persist("/test_job/config", "");
    }
    
    @Test
    public void assertCreateJobNodeIfNotNeeded() {
        when(coordinatorRegistryCenter.isExisted("/test_job")).thenReturn(true);
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(coordinatorRegistryCenter).isExisted("/test_job");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config");
        verify(coordinatorRegistryCenter, times(0)).persist("/test_job/config", "");
    }
    
    @Test
    public void assertRemoveJobNodeIfNeeded() {
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config");
        verify(coordinatorRegistryCenter).remove("/test_job/config");
    }
    
    @Test
    public void assertRemoveJobNodeIfNotNeeded() {
        when(coordinatorRegistryCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config");
        verify(coordinatorRegistryCenter, times(0)).remove("/test_job/config");
    }
    
    @Test
    public void assertFillJobNodeIfNotNullAndOverwriteDisabled() {
        when(coordinatorRegistryCenter.isExisted("/test_job/config/cron")).thenReturn(true);
        jobNodeStorage.fillJobNodeIfNullOrOverwrite("config/cron", "0/1 * * * * ?");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config/cron");
        verify(coordinatorRegistryCenter, times(0)).persist("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertFillJobNodeIfNotNullAndOverwriteEnabledButValueSame() throws NoSuchFieldException {
        when(coordinatorRegistryCenter.isExisted("/test_job/config/cron")).thenReturn(true);
        when(coordinatorRegistryCenter.getDirectly("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        JobConfigurationUtil.setFieldValue(liteJobConfig, "overwrite", true);
        jobNodeStorage.fillJobNodeIfNullOrOverwrite("config/cron", "0/1 * * * * ?");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config/cron");
        verify(coordinatorRegistryCenter).getDirectly("/test_job/config/cron");
        verify(coordinatorRegistryCenter, times(0)).persist("/test_job/config", "0/1 * * * * ?");
    }
    
    @Test
    public void assertFillJobNodeIfNotNullAndOverwriteEnabledAndValueDifferent() throws NoSuchFieldException {
        when(coordinatorRegistryCenter.isExisted("/test_job/config/cron")).thenReturn(true);
        when(coordinatorRegistryCenter.getDirectly("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        JobConfigurationUtil.setFieldValue(liteJobConfig, "overwrite", true);
        jobNodeStorage.fillJobNodeIfNullOrOverwrite("config/cron", "0/2 * * * * ?");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config/cron");
        verify(coordinatorRegistryCenter).getDirectly("/test_job/config/cron");
        verify(coordinatorRegistryCenter).persist("/test_job/config/cron", "0/2 * * * * ?");
    }
    
    @Test
    public void assertFillJobNodeIfNull() {
        when(coordinatorRegistryCenter.isExisted("/test_job/config/cron")).thenReturn(false);
        jobNodeStorage.fillJobNodeIfNullOrOverwrite("config/cron", "0/2 * * * * ?");
        verify(coordinatorRegistryCenter).isExisted("/test_job/config/cron");
        verify(coordinatorRegistryCenter).persist("/test_job/config/cron", "0/2 * * * * ?");
    }
    
    @Test
    public void assertFillEphemeralJobNode() {
        jobNodeStorage.fillEphemeralJobNode("config/cron", "0/1 * * * * ?");
        verify(coordinatorRegistryCenter).persistEphemeral("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertUpdateJobNode() {
        jobNodeStorage.updateJobNode("config/cron", "0/1 * * * * ?");
        verify(coordinatorRegistryCenter).update("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertReplaceJobNode() {
        jobNodeStorage.replaceJobNode("config/cron", "0/1 * * * * ?");
        verify(coordinatorRegistryCenter).persist("/test_job/config/cron", "0/1 * * * * ?");
    }
    
    @Test
    public void assertExecuteInTransactionSuccess() throws Exception {
        CuratorFramework client = mock(CuratorFramework.class);
        CuratorTransaction curatorTransaction = mock(CuratorTransaction.class);
        TransactionCheckBuilder transactionCheckBuilder = mock(TransactionCheckBuilder.class);
        CuratorTransactionBridge curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        CuratorTransactionFinal curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        when(coordinatorRegistryCenter.getRawClient()).thenReturn(client);
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
        verify(coordinatorRegistryCenter).getRawClient();
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
        when(coordinatorRegistryCenter.getRawClient()).thenReturn(client);
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
        verify(coordinatorRegistryCenter).getRawClient();
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
        when(coordinatorRegistryCenter.getRawClient()).thenReturn(client);
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
        when(coordinatorRegistryCenter.getRawCache("/test_job")).thenReturn(treeCache);
        jobNodeStorage.addDataListener(listener);
        verify(listeners).addListener(listener);
    }
    
    @Test
    public void assertGetRegistryCenterTime() {
        when(coordinatorRegistryCenter.getRegistryCenterTime("/test_job/systemTime/current")).thenReturn(0L);
        assertThat(jobNodeStorage.getRegistryCenterTime(), is(0L));
        verify(coordinatorRegistryCenter).getRegistryCenterTime("/test_job/systemTime/current");
    }
    
    @Test
    public void assertGetJobConfiguration() {
        assertThat(jobNodeStorage.getLiteJobConfig(), is(liteJobConfig));
    }
}
