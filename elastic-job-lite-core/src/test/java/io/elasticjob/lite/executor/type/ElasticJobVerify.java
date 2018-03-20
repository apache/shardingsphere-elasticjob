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

package io.elasticjob.lite.executor.type;

import io.elasticjob.lite.event.type.JobStatusTraceEvent.State;
import io.elasticjob.lite.exception.JobExecutionEnvironmentException;
import io.elasticjob.lite.executor.JobFacade;
import io.elasticjob.lite.executor.ShardingContexts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ElasticJobVerify {
    
    public static void prepareForIsNotMisfire(final JobFacade jobFacade, final ShardingContexts shardingContexts) {
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
    }
    
    public static void verifyForIsNotMisfire(final JobFacade jobFacade, final ShardingContexts shardingContexts) {
        try {
            verify(jobFacade).checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException ex) {
            throw new RuntimeException(ex);
        }
        verify(jobFacade).getShardingContexts();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobFacade).beforeJobExecuted(shardingContexts);
        verify(jobFacade).registerJobBegin(shardingContexts);
        verify(jobFacade).registerJobCompleted(shardingContexts);
        verify(jobFacade).isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet());
        verify(jobFacade).afterJobExecuted(shardingContexts);
    }
}
