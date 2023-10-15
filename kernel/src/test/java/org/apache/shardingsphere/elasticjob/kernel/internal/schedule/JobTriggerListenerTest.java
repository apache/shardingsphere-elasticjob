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

package org.apache.shardingsphere.elasticjob.kernel.internal.schedule;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTriggerListenerTest {
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private Trigger trigger;
    
    private JobTriggerListener jobTriggerListener;
    
    @BeforeEach
    void setUp() {
        jobTriggerListener = new JobTriggerListener(executionService, shardingService);
    }
    
    @Test
    void assertGetName() {
        assertThat(jobTriggerListener.getName(), is("JobTriggerListener"));
    }
    
    @Test
    void assertTriggerMisfiredWhenPreviousFireTimeIsNull() {
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService, times(0)).setMisfire(Collections.singletonList(0));
    }
    
    @Test
    void assertTriggerMisfiredWhenPreviousFireTimeIsNotNull() {
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(trigger.getPreviousFireTime()).thenReturn(new Date());
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService).setMisfire(Collections.singletonList(0));
    }
}
