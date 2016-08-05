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

package com.dangdang.ddframe.job.api.config.impl;

import com.dangdang.ddframe.job.api.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.api.fixture.APIJsonConstants;
import com.dangdang.ddframe.job.api.fixture.config.TestDataflowJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.config.TestJobRootConfiguration;
import com.dangdang.ddframe.job.api.fixture.config.TestScriptJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.config.TestSimpleJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.handler.IgnoreJobExceptionHandler;
import com.dangdang.ddframe.job.api.fixture.handler.ThrowJobExceptionHandler;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobConfigurationGsonTypeAdapterTest {
    
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(TestJobRootConfiguration.class, new JobConfigurationGsonTypeAdapter()).create();
    
    @Test
    public void assertToSimpleJobJson() {
        assertThat(GSON.toJson(new TestJobRootConfiguration(new TestSimpleJobConfiguration().getTypeConfig())), 
                is(APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToDataflowJobJson() {
        assertThat(GSON.toJson(new TestJobRootConfiguration(new TestDataflowJobConfiguration(DataflowJobConfiguration.DataflowType.SEQUENCE, true, 10).getTypeConfig())),
                is(APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToScriptJobJson() {
        assertThat(GSON.toJson(new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh").getTypeConfig())),
                is(APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertFromSimpleJobJson() {
        TestJobRootConfiguration actual = GSON.fromJson(
                APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestSimpleJobConfiguration().getTypeConfig());
        assertThat(GSON.toJson(actual), is(GSON.toJson(expected)));
    }
    
    @Test
    public void assertFromDataflowJobJson() {
        TestJobRootConfiguration actual = GSON.fromJson(
                APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestDataflowJobConfiguration(DataflowJobConfiguration.DataflowType.SEQUENCE, true, 10).getTypeConfig());
        assertThat(GSON.toJson(actual), is(GSON.toJson(expected)));
    }
    
    @Test
    public void assertFromScriptJobJson() {
        TestJobRootConfiguration actual = GSON.fromJson(
                APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh").getTypeConfig());
        assertThat(GSON.toJson(actual), is(GSON.toJson(expected)));
    }
    
    public static class JobConfigurationGsonTypeAdapter extends AbstractJobConfigurationGsonTypeAdapter<TestJobRootConfiguration> {
    
        @Override
        protected void addToCustomizedValueMap(final String jsonName, final JsonReader in, final Map<String, Object> customizedValueMap) throws IOException {
        }
    
        @Override
        protected TestJobRootConfiguration getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap) {
            return new TestJobRootConfiguration(typeConfig);
        }
    
        @Override
        protected void writeCustomized(final JsonWriter out, final TestJobRootConfiguration value) throws IOException {
        }
    }
}
