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

package com.dangdang.ddframe.job.util.json;

import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.fixture.APIJsonConstants;
import com.dangdang.ddframe.job.fixture.config.TestDataflowJobConfiguration;
import com.dangdang.ddframe.job.fixture.config.TestJobRootConfiguration;
import com.dangdang.ddframe.job.fixture.config.TestScriptJobConfiguration;
import com.dangdang.ddframe.job.fixture.config.TestSimpleJobConfiguration;
import com.dangdang.ddframe.job.fixture.handler.IgnoreJobExceptionHandler;
import com.dangdang.ddframe.job.fixture.handler.ThrowJobExceptionHandler;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobConfigurationGsonTypeAdapterTest {
    
    @BeforeClass
    public static void setUp() {
        GsonFactory.registerTypeAdapter(TestJobRootConfiguration.class, new JobConfigurationGsonTypeAdapter());
    } 
    
    @Test
    public void assertToSimpleJobJson() {
        assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(
                new TestSimpleJobConfiguration(ThrowJobExceptionHandler.class.getCanonicalName(), DefaultExecutorServiceHandler.class.getCanonicalName()).getTypeConfig())),
                is(APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToDataflowJobJson() {
        assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(new TestDataflowJobConfiguration(true).getTypeConfig())),
                is(APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToScriptJobJson() {
        assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh", ThrowJobExceptionHandler.class).getTypeConfig())),
                is(APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertFromSimpleJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(
                new TestSimpleJobConfiguration(ThrowJobExceptionHandler.class.getCanonicalName(), DefaultExecutorServiceHandler.class.getCanonicalName()).getTypeConfig());
        assertThat(GsonFactory.getGson().toJson(actual), is(GsonFactory.getGson().toJson(expected)));
    }
    
    @Test
    public void assertFromDataflowJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestDataflowJobConfiguration(true).getTypeConfig());
        assertThat(GsonFactory.getGson().toJson(actual), is(GsonFactory.getGson().toJson(expected)));
    }
    
    @Test
    public void assertFromScriptJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh", ThrowJobExceptionHandler.class).getTypeConfig());
        assertThat(GsonFactory.getGson().toJson(actual), is(GsonFactory.getGson().toJson(expected)));
    }
    
    private static class JobConfigurationGsonTypeAdapter extends AbstractJobConfigurationGsonTypeAdapter<TestJobRootConfiguration> {
    
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
