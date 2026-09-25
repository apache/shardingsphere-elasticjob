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

package org.apache.shardingsphere.elasticjob.kernel.listener;

import org.apache.shardingsphere.elasticjob.kernel.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractDistributeOnceElasticJobListenerBoundedWaitTest {
    
    @Test
    void assertBeforeJobExecutedRestoresInterruptStatus() {
        TestListener listener = new TestListener();
        GuaranteeService guaranteeService = mock(GuaranteeService.class);
        when(guaranteeService.isRegisterStartSuccess(any())).thenReturn(true);
        when(guaranteeService.isAllStarted()).thenReturn(false);
        listener.setGuaranteeService(guaranteeService);
        ShardingContexts shardingContexts = createShardingContexts();
        Thread.currentThread().interrupt();
        try {
            listener.beforeJobExecuted(shardingContexts);
            assertTrue(Thread.currentThread().isInterrupted(), "interrupt status should be restored after an interrupted wait");
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void assertAfterJobExecutedRestoresInterruptStatus() {
        TestListener listener = new TestListener();
        GuaranteeService guaranteeService = mock(GuaranteeService.class);
        when(guaranteeService.isRegisterCompleteSuccess(any())).thenReturn(true);
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        listener.setGuaranteeService(guaranteeService);
        ShardingContexts shardingContexts = createShardingContexts();
        Thread.currentThread().interrupt();
        try {
            listener.afterJobExecuted(shardingContexts);
            assertTrue(Thread.currentThread().isInterrupted(), "interrupt status should be restored after an interrupted wait");
        } finally {
            Thread.interrupted();
        }
    }
    
    private ShardingContexts createShardingContexts() {
        ShardingContexts result = mock(ShardingContexts.class);
        when(result.getShardingItemParameters()).thenReturn(Collections.singletonMap(0, "0"));
        return result;
    }
    
    private static final class TestListener extends AbstractDistributeOnceElasticJobListener {
        
        TestListener() {
            super(60_000L, 60_000L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
        }
        
        @Override
        public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        }
        
        @Override
        public String getType() {
            return "TestListener";
        }
    }
}
