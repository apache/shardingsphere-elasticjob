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

package org.apache.shardingsphere.elasticjob.kernel.internal.election;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderServiceBoundedWaitTest {
    
    @Test
    void assertIsLeaderUntilBlockExitsWithinBoundedTime() throws ReflectiveOperationException, InterruptedException {
        LeaderService leaderService = createLeaderServiceWithNoLeader(500L);
        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = startWorker(leaderService, returned);
        worker.join(3000L);
        assertTrue(returned.get(), "isLeaderUntilBlock() should give up within a bounded time instead of spinning forever");
    }
    
    @Test
    void assertIsLeaderUntilBlockExitsPromptlyOnInterrupt() throws ReflectiveOperationException, InterruptedException {
        LeaderService leaderService = createLeaderServiceWithNoLeader(60_000L);
        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = startWorker(leaderService, returned);
        Thread.sleep(150L);
        worker.interrupt();
        worker.join(2000L);
        assertTrue(returned.get(), "isLeaderUntilBlock() should exit promptly when interrupted, well before the configured timeout");
    }
    
    @Test
    void assertIsLeaderUntilBlockExitsWhenJobConfigurationIsRemoved() throws ReflectiveOperationException, InterruptedException {
        LeaderService leaderService = createLeaderServiceWithNoLeader(60_000L);
        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.load(anyBoolean())).thenThrow(new JobConfigurationException("removed"));
        setPrivateField(leaderService, "configService", configService);
        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = startWorker(leaderService, returned);
        worker.join(2000L);
        assertTrue(returned.get(), "isLeaderUntilBlock() should stop waiting without throwing when the job configuration has been removed");
    }
    
    private LeaderService createLeaderServiceWithNoLeader(final long maxWaitMillis) throws ReflectiveOperationException {
        LeaderService result = new LeaderService(mock(CoordinatorRegistryCenter.class), "bounded_wait_job");
        ServerService serverService = mock(ServerService.class);
        when(serverService.hasAvailableServers()).thenReturn(true);
        setPrivateField(result, "serverService", serverService);
        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.load(anyBoolean())).thenReturn(JobConfiguration.newBuilder("bounded_wait_job", 1).maxWaitMillis(maxWaitMillis).build());
        setPrivateField(result, "configService", configService);
        return result;
    }
    
    private Thread startWorker(final LeaderService leaderService, final AtomicBoolean returned) {
        Thread result = new Thread(() -> {
            leaderService.isLeaderUntilBlock();
            returned.set(true);
        });
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    private static void setPrivateField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
