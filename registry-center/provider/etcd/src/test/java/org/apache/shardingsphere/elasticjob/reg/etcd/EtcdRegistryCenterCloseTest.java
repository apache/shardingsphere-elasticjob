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

package org.apache.shardingsphere.elasticjob.reg.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtcdRegistryCenterCloseTest {
    
    private static final String CACHE_KEY = "/job/config";
    
    private EtcdRegistryCenter registryCenter;
    
    @Mock
    private KV kvClient;
    
    @Mock
    private Lease leaseClient;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        registryCenter = new EtcdRegistryCenter(new EtcdConfiguration("http://localhost:2379", "test"));
        Plugins.getMemberAccessor().set(EtcdRegistryCenter.class.getDeclaredField("kvClient"), registryCenter, kvClient);
        Plugins.getMemberAccessor().set(EtcdRegistryCenter.class.getDeclaredField("leaseClient"), registryCenter, leaseClient);
    }
    
    @Test
    void assertIsLeaseNotFound() {
        StatusRuntimeException notFound = new StatusRuntimeException(Status.NOT_FOUND.withDescription("etcdserver: requested lease not found"));
        assertTrue(EtcdRegistryCenter.isLeaseNotFound(new ExecutionException(notFound)));
        assertTrue(EtcdRegistryCenter.isLeaseNotFound(notFound));
        assertFalse(EtcdRegistryCenter.isLeaseNotFound(new ExecutionException(new StatusRuntimeException(Status.UNAVAILABLE))));
        assertFalse(EtcdRegistryCenter.isLeaseNotFound(new IllegalStateException("other failure")));
    }
    
    @Test
    void assertCloseToleratesExpiredLease() {
        LeaseGrantResponse grantResponse = mock(LeaseGrantResponse.class);
        when(grantResponse.getID()).thenReturn(12345L);
        when(leaseClient.grant(30L)).thenReturn(CompletableFuture.completedFuture(grantResponse));
        when(kvClient.put(any(ByteSequence.class), any(ByteSequence.class), any(PutOption.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(PutResponse.class)));
        CompletableFuture<LeaseRevokeResponse> revokeFuture = new CompletableFuture<>();
        revokeFuture.completeExceptionally(new StatusRuntimeException(Status.NOT_FOUND.withDescription("etcdserver: requested lease not found")));
        when(leaseClient.revoke(12345L)).thenReturn(revokeFuture);
        registryCenter.persistEphemeral(CACHE_KEY, "value");
        registryCenter.close();
        verify(leaseClient).revoke(12345L);
    }
}
