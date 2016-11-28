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

package com.dangdang.ddframe.job.util.concurrent;

import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class ExecutorServiceObjectTest {
    
    private ExecutorServiceObject executorServiceObject;
    
    @Test
    public void assertCreateExecutorService() {
        executorServiceObject = new ExecutorServiceObject("executor-service-test", 1);
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
    
    class FooTask implements Runnable {
        
        @Override
        public void run() {
            BlockUtils.sleep(1000L);
        }
    }
}
