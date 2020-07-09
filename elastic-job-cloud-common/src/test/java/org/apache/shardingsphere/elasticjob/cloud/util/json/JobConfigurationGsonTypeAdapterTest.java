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

package org.apache.shardingsphere.elasticjob.cloud.util.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.APIJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestDataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestJobRootConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestSimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.impl.DefaultExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.cloud.fixture.handler.IgnoreJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.fixture.handler.ThrowJobExceptionHandler;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public final class JobConfigurationGsonTypeAdapterTest {
    
    @BeforeClass
    public static void setUp() {
        GsonFactory.registerTypeAdapter(TestJobRootConfiguration.class, new JobConfigurationGsonTypeAdapter());
    } 
    
    @Test
    public void assertToSimpleJobJson() {
        Assert.assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(
                new TestSimpleJobConfiguration(ThrowJobExceptionHandler.class.getCanonicalName(), DefaultExecutorServiceHandler.class.getCanonicalName()).getTypeConfig())),
                Is.is(APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToDataflowJobJson() {
        Assert.assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(new TestDataflowJobConfiguration(true).getTypeConfig())),
                Is.is(APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertToScriptJobJson() {
        Assert.assertThat(GsonFactory.getGson().toJson(new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh", ThrowJobExceptionHandler.class).getTypeConfig())),
                Is.is(APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertFromSimpleJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getSimpleJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(
                new TestSimpleJobConfiguration(ThrowJobExceptionHandler.class.getCanonicalName(), DefaultExecutorServiceHandler.class.getCanonicalName()).getTypeConfig());
        Assert.assertThat(GsonFactory.getGson().toJson(actual), Is.is(GsonFactory.getGson().toJson(expected)));
    }
    
    @Test
    public void assertFromDataflowJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getDataflowJobJson(IgnoreJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestDataflowJobConfiguration(true).getTypeConfig());
        Assert.assertThat(GsonFactory.getGson().toJson(actual), Is.is(GsonFactory.getGson().toJson(expected)));
    }
    
    @Test
    public void assertFromScriptJobJson() {
        TestJobRootConfiguration actual = GsonFactory.getGson().fromJson(
                APIJsonConstants.getScriptJobJson(ThrowJobExceptionHandler.class.getCanonicalName()), TestJobRootConfiguration.class);
        TestJobRootConfiguration expected = new TestJobRootConfiguration(new TestScriptJobConfiguration("test.sh", ThrowJobExceptionHandler.class).getTypeConfig());
        Assert.assertThat(GsonFactory.getGson().toJson(actual), Is.is(GsonFactory.getGson().toJson(expected)));
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
