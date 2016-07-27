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

import com.dangdang.ddframe.job.api.type.dataflow.executor.sequence.StreamingSequenceDataflowJobTest;
import com.dangdang.ddframe.job.api.type.dataflow.executor.sequence.UnstreamingSequenceDataflowJobTest;
import com.dangdang.ddframe.job.api.type.dataflow.executor.throughput.StreamingThroughputDataflowJobTest;
import com.dangdang.ddframe.job.api.type.dataflow.executor.throughput.UnstreamingThroughputDataflowJobTest;
import com.dangdang.ddframe.job.api.type.script.executor.ScriptJobExecutorTest;
import com.dangdang.ddframe.job.api.type.simple.executor.SimpleJobExecutorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SimpleJobExecutorTest.class,
        StreamingSequenceDataflowJobTest.class, 
        StreamingThroughputDataflowJobTest.class, 
        UnstreamingSequenceDataflowJobTest.class, 
        UnstreamingThroughputDataflowJobTest.class, 
        ScriptJobExecutorTest.class
    })
public final class AllTypeTests {
}
