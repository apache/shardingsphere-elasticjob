package org.apache.shardingsphere.elasticjob.kernel.listener;

import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.kernel.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractDistributeOnceElasticJobListenerBoundedWaitTest {

    private static final class TestListener extends AbstractDistributeOnceElasticJobListener {
        TestListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
            super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
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

    @Test
    void assertBeforeJobExecutedExitsWithinBoundedTimeWhenRegisterNeverSucceeds() throws Exception {
        TestListener listener = new TestListener(500L, 500L);

        GuaranteeService mockGuaranteeService = mock(GuaranteeService.class);
        // Registration never succeeds -> pre-fix, this spins forever before ever reaching the wait()/timeout logic.
        when(mockGuaranteeService.isRegisterStartSuccess(any())).thenReturn(false);
        listener.setGuaranteeService(mockGuaranteeService);

        ShardingContexts shardingContexts = mock(ShardingContexts.class);
        when(shardingContexts.getShardingItemParameters()).thenReturn(Collections.singletonMap(0, "0"));

        AtomicBoolean returned = new AtomicBoolean(false);
        AtomicReference<Throwable> caught = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                listener.beforeJobExecuted(shardingContexts);
                returned.set(true);
            } catch (Throwable ex) {
                caught.set(ex);
            }
        });
        worker.setDaemon(true);
        worker.start();
        worker.join(3000);

        assertTrue(returned.get() || caught.get() instanceof JobSystemException,
                "beforeJobExecuted() should give up (via timeout exception) within a bounded time instead of spinning forever");
        verify(mockGuaranteeService, times(1)).clearAllStartedInfo();
    }
}