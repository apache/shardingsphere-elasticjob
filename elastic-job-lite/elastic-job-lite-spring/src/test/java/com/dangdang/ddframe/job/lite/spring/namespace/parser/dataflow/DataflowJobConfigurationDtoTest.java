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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.dataflow;

import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.ThroughputDataflowElasticJob;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataflowJobConfigurationDtoTest {
    
    @Test
    public void assertToLiteJobConfigurationForAllProperties() {
        LiteJobConfiguration actual = new DataflowJobConfigurationDto(
                "dataflowJob", "0/1 * * * * ?", 10, ThroughputDataflowElasticJob.class, DataflowJobConfiguration.DataflowType.THROUGHPUT, true, 10).toLiteJobConfiguration();
        assertThat(actual.getJobName(), is("dataflowJob"));
        assertThat(((DataflowJobConfiguration) actual.getTypeConfig()).getDataflowType(), is(DataflowJobConfiguration.DataflowType.THROUGHPUT));
        assertTrue(((DataflowJobConfiguration) actual.getTypeConfig()).isStreamingProcess());
        assertThat(((DataflowJobConfiguration) actual.getTypeConfig()).getConcurrentDataProcessThreadCount(), is(10));
    }
    
    @Test
    public void assertToLiteJobConfigurationWhenStreamingProcessAndConcurrentDataProcessThreadCountIsAllNull() {
        LiteJobConfiguration actual = new DataflowJobConfigurationDto(
                "dataflowJob", "0/1 * * * * ?", 10, ThroughputDataflowElasticJob.class, DataflowJobConfiguration.DataflowType.THROUGHPUT, null, null).toLiteJobConfiguration();
        assertThat(actual.getJobName(), is("dataflowJob"));
        assertFalse(((DataflowJobConfiguration) actual.getTypeConfig()).isStreamingProcess());
        assertThat(((DataflowJobConfiguration) actual.getTypeConfig()).getConcurrentDataProcessThreadCount(), is(Runtime.getRuntime().availableProcessors() * 2));
    }
}
