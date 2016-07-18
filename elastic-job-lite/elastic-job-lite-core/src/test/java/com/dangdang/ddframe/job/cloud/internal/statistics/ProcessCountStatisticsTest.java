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

package com.dangdang.ddframe.job.cloud.internal.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public final class ProcessCountStatisticsTest {
    
    @Test
    public void assertProcessSuccessCount() {
        ProcessCountStatistics.incrementProcessSuccessCount("testJob");
        ProcessCountStatistics.incrementProcessSuccessCount("testJob");
        assertThat(ProcessCountStatistics.getProcessSuccessCount("testJob"), is(2));
        assertThat(ProcessCountStatistics.getProcessSuccessCount("otherJob"), is(0));
    }
    
    @Test
    public void assertProcessFailureCount() {
        ProcessCountStatistics.incrementProcessFailureCount("testJob");
        ProcessCountStatistics.incrementProcessFailureCount("testJob");
        assertThat(ProcessCountStatistics.getProcessFailureCount("testJob"), is(2));
        assertThat(ProcessCountStatistics.getProcessFailureCount("otherJob"), is(0));
    }
    
    @Test
    public void assertReset() {
        ProcessCountStatistics.incrementProcessSuccessCount("testJob");
        ProcessCountStatistics.incrementProcessFailureCount("testJob");
        ProcessCountStatistics.reset("testJob");
        ProcessCountStatistics.reset("otherJob");
        assertThat(ProcessCountStatistics.getProcessSuccessCount("testJob"), is(0));
        assertThat(ProcessCountStatistics.getProcessSuccessCount("otherJob"), is(0));
        assertThat(ProcessCountStatistics.getProcessFailureCount("testJob"), is(0));
        assertThat(ProcessCountStatistics.getProcessFailureCount("otherJob"), is(0));
    }
}
