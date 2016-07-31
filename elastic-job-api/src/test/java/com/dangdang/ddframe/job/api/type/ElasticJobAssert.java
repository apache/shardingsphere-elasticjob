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

package com.dangdang.ddframe.job.api.type;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticJobAssert {
    
    public static final String JOB_NAME = "unit_test_job";
    
    public static ShardingContext getShardingContext() {
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "A");
        map.put(1, "B");
        return new ShardingContext(JOB_NAME, 10, "", map);
    }
    
    public static void prepareForIsNotMisfire(final JobFacade jobFacade, final ShardingContext shardingContext) {
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
    }
    
    public static void verifyForIsNotMisfire(final JobFacade jobFacade, final ShardingContext shardingContext) {
        try {
            verify(jobFacade).checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException ex) {
            throw new RuntimeException(ex);
        }
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItemParameters().keySet());
        verify(jobFacade).cleanPreviousExecutionInfo();
        verify(jobFacade).beforeJobExecuted(shardingContext);
        verify(jobFacade).registerJobBegin(shardingContext);
        verify(jobFacade).registerJobCompleted(shardingContext);
        verify(jobFacade).isExecuteMisfired(shardingContext.getShardingItemParameters().keySet());
        verify(jobFacade).afterJobExecuted(shardingContext);
    }
}
