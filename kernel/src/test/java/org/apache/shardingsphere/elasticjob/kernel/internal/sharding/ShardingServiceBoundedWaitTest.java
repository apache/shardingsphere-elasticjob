package org.apache.shardingsphere.elasticjob.kernel.internal.sharding;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingServiceBoundedWaitTest {

    @Test
    void assertBlockUntilShardingCompletedExitsWithinBoundedTime() throws Exception {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        ShardingService shardingService = new ShardingService(regCenter, "test_job");

        LeaderService mockLeaderService = mock(LeaderService.class);
        when(mockLeaderService.isLeaderUntilBlock()).thenReturn(false);
        setPrivateField(shardingService, "leaderService", mockLeaderService);

        JobNodeStorage mockJobNodeStorage = mock(JobNodeStorage.class);
        // Simulates the zombie state: leader's failed transaction never cleared PROCESSING.
        when(mockJobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY)).thenReturn(true);
        when(mockJobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING)).thenReturn(true);
        setPrivateField(shardingService, "jobNodeStorage", mockJobNodeStorage);

        ConfigurationService mockConfigService = mock(ConfigurationService.class);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 1).maxWaitMillis(500L).build();
        when(mockConfigService.load(anyBoolean())).thenReturn(jobConfig);
        setPrivateField(shardingService, "configService", mockConfigService);

        Method blockUntilShardingCompleted = ShardingService.class.getDeclaredMethod("blockUntilShardingCompleted");
        blockUntilShardingCompleted.setAccessible(true);

        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                blockUntilShardingCompleted.invoke(shardingService);
                returned.set(true);
            } catch (Exception ignored) {
                // reflection wrapper exception, not relevant here
            }
        });
        worker.setDaemon(true);
        worker.start();
        worker.join(3000);

        assertTrue(returned.get(), "blockUntilShardingCompleted() should give up within a bounded time instead of spinning forever");
    }

    @Test
    void assertBlockUntilShardingCompletedExitsPromptlyOnInterrupt() throws Exception {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        ShardingService shardingService = new ShardingService(regCenter, "test_job");

        LeaderService mockLeaderService = mock(LeaderService.class);
        when(mockLeaderService.isLeaderUntilBlock()).thenReturn(false);
        setPrivateField(shardingService, "leaderService", mockLeaderService);

        JobNodeStorage mockJobNodeStorage = mock(JobNodeStorage.class);
        when(mockJobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY)).thenReturn(true);
        when(mockJobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING)).thenReturn(true);
        setPrivateField(shardingService, "jobNodeStorage", mockJobNodeStorage);

        ConfigurationService mockConfigService = mock(ConfigurationService.class);
        // Long timeout on purpose: a fast exit here can only come from the interrupt, not the deadline.
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 1).maxWaitMillis(60_000L).build();
        when(mockConfigService.load(anyBoolean())).thenReturn(jobConfig);
        setPrivateField(shardingService, "configService", mockConfigService);

        Method blockUntilShardingCompleted = ShardingService.class.getDeclaredMethod("blockUntilShardingCompleted");
        blockUntilShardingCompleted.setAccessible(true);

        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                blockUntilShardingCompleted.invoke(shardingService);
                returned.set(true);
            } catch (Exception ignored) {
                // reflection wrapper exception, not relevant here
            }
        });
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(150L);
        worker.interrupt();
        worker.join(2000);

        assertTrue(returned.get(), "blockUntilShardingCompleted() should exit promptly when interrupted, well before the configured timeout");
    }

    private static void setPrivateField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}