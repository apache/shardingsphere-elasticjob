/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.api;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class JobExecutionMultipleShardingContextTest {
    
    @Test
    public void assertCreateJobExecutionSingleShardingContext() {
        JobExecutionMultipleShardingContext shardingContext = createShardingContext();
        JobExecutionSingleShardingContext actual = shardingContext.createJobExecutionSingleShardingContext(0);
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(10));
        assertNull(actual.getJobParameter());
        assertFalse(actual.isMonitorExecution());
        assertThat(actual.getFetchDataCount(), is(0));
        assertThat(actual.getShardingItem(), is(0));
        assertThat(actual.getShardingItemParameter(), is("param0"));
        assertThat(actual.getOffset(), is("offset0"));
    }
    
    @Test
    public void assertToString() {
        assertThat(createShardingContext().toString(), is("jobName: testJob, shardingTotalCount: 10, shardingItems: [0, 1], shardingItemParameters: {0=param0}, jobParameter: null"));
    }
    
    private JobExecutionMultipleShardingContext createShardingContext() {
        JobExecutionMultipleShardingContext result = new JobExecutionMultipleShardingContext();
        result.setJobName("testJob");
        result.setShardingTotalCount(10);
        result.setShardingItems(Arrays.asList(0, 1));
        result.getShardingItemParameters().put(0, "param0");
        Map<Integer, String> offsets = new HashMap<>(2);
        offsets.put(0, "offset0");
        offsets.put(1, "offset1");
        result.setOffsets(offsets);
        return result;
    }
}
