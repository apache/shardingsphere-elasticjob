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

package com.dangdang.ddframe.job.lite.integrate.std.dataflow.sequence;

import com.dangdang.ddframe.job.api.type.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.integrate.AbstractBaseStdJobAutoInitTest;
import com.dangdang.ddframe.job.lite.integrate.WaitingUtils;
import com.dangdang.ddframe.job.lite.integrate.fixture.dataflow.sequence.StreamingSequenceDataflowElasticJob;
import com.dangdang.ddframe.job.lite.util.JobConfigurationFieldUtil;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class StreamingSequenceDataflowElasticJobTest extends AbstractBaseStdJobAutoInitTest {
    
    public StreamingSequenceDataflowElasticJobTest() {
        super(StreamingSequenceDataflowElasticJob.class, Optional.of(DataflowJobConfiguration.DataflowType.SEQUENCE));
    }
    
    @Before
    @After
    public void reset() {
        StreamingSequenceDataflowElasticJob.reset();
    }
    
    @Override
    protected void setLiteJobConfig(final LiteJobConfiguration liteJobConfig) {
        JobConfigurationFieldUtil.setFieldValue(liteJobConfig.getJobConfig(), "streamingProcess", true);
    }
    
    @Test
    public void assertJobInit() {
        while (!StreamingSequenceDataflowElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(getRegCenter().isExisted("/" + getJobName() + "/execution"));
    }
}
