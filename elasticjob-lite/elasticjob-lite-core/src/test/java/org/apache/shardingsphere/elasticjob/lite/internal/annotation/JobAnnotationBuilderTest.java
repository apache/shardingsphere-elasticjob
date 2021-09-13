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

package org.apache.shardingsphere.elasticjob.lite.internal.annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.AnnotationSimpleJob;
import org.junit.Test;

public final class JobAnnotationBuilderTest {
   
    @Test
    public void assertGenerateJobConfiguration() {
        JobConfiguration jobConfiguration = JobAnnotationBuilder.generateJobConfiguration(AnnotationSimpleJob.class);
        assertThat(jobConfiguration.getJobName(), is("AnnotationSimpleJob"));
        assertThat(jobConfiguration.getShardingTotalCount(), is(3));
        assertThat(jobConfiguration.getShardingItemParameters(), is("0=a,1=b,2=c"));
        assertThat(jobConfiguration.getCron(), is("*/10 * * * * ?"));
        assertTrue(jobConfiguration.isMonitorExecution());
        assertFalse(jobConfiguration.isFailover());
        assertTrue(jobConfiguration.isMisfire());
        assertThat(jobConfiguration.getMaxTimeDiffSeconds(), is(-1));
        assertThat(jobConfiguration.getReconcileIntervalMinutes(), is(10));
        assertThat(jobConfiguration.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(jobConfiguration.getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(jobConfiguration.getJobErrorHandlerType(), is(""));
        assertThat(jobConfiguration.getDescription(), is("desc"));
        assertThat(jobConfiguration.getProps().getProperty("print.title"), is("test title"));
        assertThat(jobConfiguration.getProps().getProperty("print.content"), is("test content"));
        assertFalse(jobConfiguration.isDisabled());
        assertFalse(jobConfiguration.isOverwrite());
    }

}
