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

package io.elasticjob.lite.integrate;

import io.elasticjob.lite.integrate.std.dataflow.OneOffDataflowElasticJobTest;
import io.elasticjob.lite.integrate.std.dataflow.StreamingDataflowElasticJobForExecuteFailureTest;
import io.elasticjob.lite.integrate.std.dataflow.StreamingDataflowElasticJobForExecuteThrowsExceptionTest;
import io.elasticjob.lite.integrate.std.dataflow.StreamingDataflowElasticJobForMultipleThreadsTest;
import io.elasticjob.lite.integrate.std.dataflow.StreamingDataflowElasticJobForNotMonitorTest;
import io.elasticjob.lite.integrate.std.dataflow.StreamingDataflowElasticJobTest;
import io.elasticjob.lite.integrate.std.script.ScriptElasticJobTest;
import io.elasticjob.lite.integrate.std.simple.DisabledJobTest;
import io.elasticjob.lite.integrate.std.simple.SimpleElasticJobTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        DisabledJobTest.class, 
        SimpleElasticJobTest.class, 
        OneOffDataflowElasticJobTest.class, 
        StreamingDataflowElasticJobTest.class, 
        StreamingDataflowElasticJobForNotMonitorTest.class, 
        StreamingDataflowElasticJobForMultipleThreadsTest.class, 
        StreamingDataflowElasticJobForExecuteFailureTest.class, 
        StreamingDataflowElasticJobForExecuteThrowsExceptionTest.class, 
        ScriptElasticJobTest.class
    })
public final class AllIntegrateTests {
}
