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

package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.sharding.ExecutionService;
import io.elasticjob.lite.internal.sharding.ShardingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobTriggerListenerTest {
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private Trigger trigger;
    
    private JobTriggerListener jobTriggerListener;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        jobTriggerListener = new JobTriggerListener(executionService, shardingService);
    }
    
    @Test
    public void assertGetName() {
        assertThat(jobTriggerListener.getName(), is("JobTriggerListener"));
    }
    
    @Test
    public void assertTriggerMisfiredWhenPreviousFireTimeIsNull() {
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService, times(0)).setMisfire(Collections.singletonList(0));
    }
    
    @Test
    public void assertTriggerMisfiredWhenPreviousFireTimeIsNotNull() {
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(trigger.getPreviousFireTime()).thenReturn(new Date());
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService).setMisfire(Collections.singletonList(0));
    }
}
