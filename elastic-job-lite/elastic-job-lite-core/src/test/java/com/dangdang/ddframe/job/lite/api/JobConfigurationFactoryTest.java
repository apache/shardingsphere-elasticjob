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

package com.dangdang.ddframe.job.lite.api;

import com.dangdang.ddframe.job.api.job.dataflow.DataflowType;
import com.dangdang.ddframe.job.lite.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.lite.api.config.impl.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.lite.integrate.fixture.simple.SimpleElasticJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobConfigurationFactoryTest {
    
    @Test
    public void assertCreateSimpleJobConfiguration() {
        assertThat(JobConfigurationFactory.createSimpleJobConfigurationBuilder("simpleJob", SimpleElasticJob.class, 10, "0/10 * * * *").build(), is(instanceOf(SimpleJobConfiguration.class)));
    }

    @Test
    public void assertCreateDataflowJobConfiguration() {
        assertThat(JobConfigurationFactory.createDataflowJobConfigurationBuilder("dataflowJob", TestDataflowJob.class, 10, "0/10 * * * *", DataflowType.THROUGHPUT).build(), 
                is(instanceOf(DataflowJobConfiguration.class)));
    }

    @Test
    public void assertCreateScriptJobConfiguration() {
        assertThat(JobConfigurationFactory.createScriptJobConfigurationBuilder("scriptJob", 10, "0/10 * * * *", "test.sh").build(), is(instanceOf(ScriptJobConfiguration.class)));
    }
}
