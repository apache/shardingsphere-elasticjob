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

package org.apache.shardingsphere.elasticjob.infra.concurrent;

import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ElasticJobExecutorServiceTest {
    
    @Test
    public void assertCreateExecutorService() {
        ElasticJobExecutorService executorServiceObject = new ElasticJobExecutorService("executor-service-test", 1);
        assertThat(executorServiceObject.getActiveThreadCount(), is(0));
        assertThat(executorServiceObject.getWorkQueueSize(), is(0));
        assertFalse(executorServiceObject.isShutdown());
        ExecutorService executorService = executorServiceObject.createExecutorService();
        executorService.submit(new FooTask());
        BlockUtils.waitingShortTime();
        assertThat(executorServiceObject.getActiveThreadCount(), is(1));
        assertThat(executorServiceObject.getWorkQueueSize(), is(0));
        assertFalse(executorServiceObject.isShutdown());
        executorService.submit(new FooTask());
        BlockUtils.waitingShortTime();
        assertThat(executorServiceObject.getActiveThreadCount(), is(1));
        assertThat(executorServiceObject.getWorkQueueSize(), is(1));
        assertFalse(executorServiceObject.isShutdown());
        executorService.shutdownNow();
        assertThat(executorServiceObject.getWorkQueueSize(), is(0));
        assertTrue(executorServiceObject.isShutdown());
    }
    
    static class FooTask implements Runnable {
        
        @Override
        public void run() {
            BlockUtils.sleep(1000L);
        }
    }
}
