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

package com.dangdang.ddframe.job.lite.lifecycle.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobStatusTest {
    
    @Test
    public void assertGetJobStatusWhenIsOK() {
        assertThat(JobBriefInfo.JobStatus.getJobStatus(10, 0, 0, 10), is(JobBriefInfo.JobStatus.OK));
    }
    
    @Test
    public void assertGetJobStatusWhenIsAllCrashed() {
        assertThat(JobBriefInfo.JobStatus.getJobStatus(0, 10, 0, 10), is(JobBriefInfo.JobStatus.ALL_CRASHED));
    }
    
    @Test
    public void assertGetJobStatusWhenIsPartialAlive() {
        assertThat(JobBriefInfo.JobStatus.getJobStatus(5, 5, 0, 10), is(JobBriefInfo.JobStatus.PARTIAL_ALIVE));
    }
    
    @Test
    public void assertGetJobStatusWhenIsDisabled() {
        assertThat(JobBriefInfo.JobStatus.getJobStatus(0, 0, 10, 10), is(JobBriefInfo.JobStatus.DISABLED));
    }
}
