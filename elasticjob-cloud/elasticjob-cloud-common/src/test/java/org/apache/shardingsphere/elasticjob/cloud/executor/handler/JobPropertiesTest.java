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

package org.apache.shardingsphere.elasticjob.cloud.executor.handler;

import org.apache.shardingsphere.elasticjob.cloud.executor.handler.impl.DefaultExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.impl.DefaultJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.fixture.APIJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.fixture.handler.IgnoreJobExceptionHandler;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

public final class JobPropertiesTest {
    
    @Test
    public void assertPutInvalidKey() throws NoSuchFieldException {
        JobProperties actual = new JobProperties();
        actual.put("invalid_key", "");
        Assert.assertTrue(getMap(actual).isEmpty());
    }
    
    @Test
    public void assertPutNullValue() throws NoSuchFieldException {
        JobProperties actual = new JobProperties();
        actual.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), null);
        Assert.assertTrue(getMap(actual).isEmpty());
    }
    
    @Test
    public void assertPutSuccess() throws NoSuchFieldException {
        JobProperties actual = new JobProperties();
        actual.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), DefaultJobExceptionHandler.class.getCanonicalName());
        Assert.assertThat(getMap(actual).size(), is(1));
    }
    
    private Map getMap(final JobProperties jobProperties) throws NoSuchFieldException {
        return (Map) ReflectionUtils.getFieldValue(jobProperties, JobProperties.class.getDeclaredField("map"));
    }
    
    @Test
    public void assertGetWhenValueIsEmpty() {
        JobProperties actual = new JobProperties();
        Assert.assertThat(actual.get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(DefaultJobExceptionHandler.class.getCanonicalName()));
        Assert.assertThat(actual.get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER), is(DefaultExecutorServiceHandler.class.getCanonicalName()));
    }
    
    @Test
    public void assertGetWhenValueIsNotEmpty() {
        JobProperties actual = new JobProperties();
        actual.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), IgnoreJobExceptionHandler.class.getCanonicalName());
        Assert.assertThat(actual.get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(IgnoreJobExceptionHandler.class.getCanonicalName()));
    }
    
    @Test
    public void assertJson() {
        Assert.assertThat(new JobProperties().json(), is(APIJsonConstants.getJobPropertiesJson(DefaultJobExceptionHandler.class.getCanonicalName())));
    }
    
    @Test
    public void assertJobPropertiesEnumFromValidValue() {
        Assert.assertThat(JobProperties.JobPropertiesEnum.from(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey()), is(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER));
    }
    
    @Test
    public void assertJobPropertiesEnumFromInvalidValue() {
        Assert.assertNull(JobProperties.JobPropertiesEnum.from("invalid"));
    }
}
