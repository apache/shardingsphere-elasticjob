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

package com.dangdang.ddframe.job.lite.integrate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.job.lite.integrate.std.dataflow.sequence.OneOffSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.sequence.StreamingSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.OneOffThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobForExecuteFailureTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobForExecuteThrowsExceptionTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobForMultipleThreadsTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobForNotMonitorTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobForPausedTest;
import com.dangdang.ddframe.job.lite.integrate.std.dataflow.throughput.StreamingThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.integrated.ScriptElasticJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.simple.DisabledJobTest;
import com.dangdang.ddframe.job.lite.integrate.std.simple.SimpleElasticJobTest;

@RunWith(Suite.class)
@SuiteClasses({
    DisabledJobTest.class, 
    SimpleElasticJobTest.class, 
    OneOffSequenceDataFlowElasticJobTest.class, 
    StreamingSequenceDataFlowElasticJobTest.class, 
    OneOffThroughputDataFlowElasticJobTest.class, 
    StreamingThroughputDataFlowElasticJobTest.class, 
    StreamingThroughputDataFlowElasticJobForNotMonitorTest.class, 
    StreamingThroughputDataFlowElasticJobForMultipleThreadsTest.class, 
    StreamingThroughputDataFlowElasticJobForExecuteFailureTest.class, 
    StreamingThroughputDataFlowElasticJobForExecuteThrowsExceptionTest.class, 
    StreamingThroughputDataFlowElasticJobForPausedTest.class,
    ScriptElasticJobTest.class
    })
public final class AllIntegrateTests {
}
