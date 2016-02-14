/**
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

package com.dangdang.ddframe.job.plugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.job.plugin.job.type.dataflow.StreamingBatchSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.StreamingBatchThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.StreamingIndividualSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.StreamingIndividualThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.UnstreamingBatchSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.UnstreamingBatchThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.UnstreamingIndividualSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.UnstreamingIndividualThroughputDataFlowElasticJobTest;
import com.dangdang.ddframe.job.plugin.job.type.simple.SimpleElasticJobTest;
import com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategyTest;
import com.dangdang.ddframe.job.plugin.sharding.strategy.OdevitySortByNameJobShardingStrategyTest;
import com.dangdang.ddframe.job.plugin.sharding.strategy.RotateServerByNameJobShardingStrategyTest;

@RunWith(Suite.class)
@SuiteClasses({
    SimpleElasticJobTest.class, 
    StreamingIndividualThroughputDataFlowElasticJobTest.class,
    StreamingBatchThroughputDataFlowElasticJobTest.class, 
    StreamingIndividualSequenceDataFlowElasticJobTest.class,
    StreamingBatchSequenceDataFlowElasticJobTest.class, 
    UnstreamingIndividualThroughputDataFlowElasticJobTest.class,
    UnstreamingBatchThroughputDataFlowElasticJobTest.class, 
    UnstreamingIndividualSequenceDataFlowElasticJobTest.class,
    UnstreamingBatchSequenceDataFlowElasticJobTest.class, 
    AverageAllocationJobShardingStrategyTest.class, 
    OdevitySortByNameJobShardingStrategyTest.class, 
    RotateServerByNameJobShardingStrategyTest.class
    })
public final class AllPluginTests {
}
