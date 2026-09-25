package org.apache.shardingsphere.elasticjob.kernel.internal.election;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderServiceBoundedWaitTest {

    @Test
    void assertIsLeaderUntilBlockExitsWithinBoundedTime() throws Exception {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        LeaderService leaderService = new LeaderService(regCenter, "test_job");

        ServerService mockServerService = mock(ServerService.class);
        // No leader ever appears, but servers are available -> loop would spin forever pre-fix.
        when(mockServerService.hasAvailableServers()).thenReturn(true);
        setPrivateField(leaderService, "serverService", mockServerService);

        ConfigurationService mockConfigService = mock(ConfigurationService.class);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 1).maxWaitMillis(500L).build();
        when(mockConfigService.load(anyBoolean())).thenReturn(jobConfig);
        setPrivateField(leaderService, "configService", mockConfigService);

        AtomicBoolean returned = new AtomicBoolean(false);
        AtomicReference<Exception> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                leaderService.isLeaderUntilBlock();
                returned.set(true);
            } catch (Exception ex) {
                failure.set(ex);
            }
        });
        worker.setDaemon(true);
        worker.start();
        worker.join(3000);

        assertTrue(returned.get(), "isLeaderUntilBlock() should give up within a bounded time instead of spinning forever"
                + (failure.get() != null ? " (threw: " + failure.get() + ")" : ""));
    }

    @Test
    void assertIsLeaderUntilBlockExitsPromptlyOnInterrupt() throws Exception {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        LeaderService leaderService = new LeaderService(regCenter, "test_job");

        ServerService mockServerService = mock(ServerService.class);
        when(mockServerService.hasAvailableServers()).thenReturn(true);
        setPrivateField(leaderService, "serverService", mockServerService);

        ConfigurationService mockConfigService = mock(ConfigurationService.class);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 1).maxWaitMillis(60_000L).build();
        when(mockConfigService.load(anyBoolean())).thenReturn(jobConfig);
        setPrivateField(leaderService, "configService", mockConfigService);

        AtomicBoolean returned = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                leaderService.isLeaderUntilBlock();
                returned.set(true);
            } catch (Exception ignored) {
                // electLeader()'s internal calls may throw against unconfigured statics; not the focus here
            }
        });
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(150L);
        worker.interrupt();
        worker.join(2000);

        assertTrue(returned.get(), "isLeaderUntilBlock() should exit promptly when interrupted, well before the configured timeout");
    }

    private static void setPrivateField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}