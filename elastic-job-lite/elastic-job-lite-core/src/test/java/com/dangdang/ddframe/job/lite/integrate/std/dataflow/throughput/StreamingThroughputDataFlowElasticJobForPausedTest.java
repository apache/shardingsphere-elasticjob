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

package com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput;

import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.integrate.AbstractBaseStdJobAutoInitTest;
import com.dangdang.ddframe.job.lite.integrate.WaitingUtils;
import com.dangdang.ddframe.job.lite.integrate.fixture.dataflow.throughput.StreamingThroughputDataflowElasticJob;
import com.dangdang.ddframe.job.lite.util.JobConfigurationFieldUtil;

import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public final class StreamingThroughputDataflowElasticJobForPausedTest extends AbstractBaseStdJobAutoInitTest {
    
    public StreamingThroughputDataflowElasticJobForPausedTest() {
        super(StreamingThroughputDataflowElasticJob.class, Optional.of(DataflowType.THROUGHPUT));
    }
    
    @Before
    @After
    public void reset() {
        StreamingThroughputDataflowElasticJob.reset();
    }
    
    @Override
    protected void setJobConfig(final JobConfiguration jobConfig) {
        JobConfigurationFieldUtil.setFieldValue(jobConfig, "streamingProcess", true);
    }
    
    @Test
    public void assertClearPausedJobStatusWhenRestartingJob() {
        while (!StreamingThroughputDataflowElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        getRegCenter().persist("/" + getJobName() + "/servers/" + getLocalHostService().getIp() + "/paused", "");
        initJob();
        assertFalse(getRegCenter().isExisted("/" + getJobName() + "/servers/" + getLocalHostService().getIp() + "/paused"));
    }
}
