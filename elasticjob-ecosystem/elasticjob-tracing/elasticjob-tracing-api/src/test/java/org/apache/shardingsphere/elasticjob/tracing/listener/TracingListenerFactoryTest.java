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

package org.apache.shardingsphere.elasticjob.tracing.listener;

import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingConfigurationException;
import org.apache.shardingsphere.elasticjob.tracing.fixture.JobEventCallerConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.fixture.TestTracingListener;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class TracingListenerFactoryTest {
    
    @Test(expected = TracingConfigurationException.class)
    public void assertGetListenerWithNullType() throws TracingConfigurationException {
        TracingListenerFactory.getListener(new TracingConfiguration<>("", null));
    }
    
    @Test(expected = TracingConfigurationException.class)
    public void assertGetInvalidListener() throws TracingConfigurationException {
        TracingListenerFactory.getListener(new TracingConfiguration<>("INVALID", null));
    }
    
    @Test
    public void assertGetListener() throws TracingConfigurationException {
        assertThat(TracingListenerFactory.getListener(new TracingConfiguration<>("TEST", new JobEventCallerConfiguration(() -> {
        }))), instanceOf(TestTracingListener.class));
    }
}
