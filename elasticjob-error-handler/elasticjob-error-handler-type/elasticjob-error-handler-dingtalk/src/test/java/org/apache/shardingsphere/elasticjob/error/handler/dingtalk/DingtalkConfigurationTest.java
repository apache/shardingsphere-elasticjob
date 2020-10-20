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

package org.apache.shardingsphere.elasticjob.error.handler.dingtalk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class DingtalkConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        DingtalkConfiguration actual = DingtalkConfiguration.newBuilder("webhook", "keyword", "secret").connectTimeoutMillisecond(4000).readTimeoutMillisecond(2000).build();
        assertThat(actual.getWebhook(), is("webhook"));
        assertThat(actual.getKeyword(), is("keyword"));
        assertThat(actual.getSecret(), is("secret"));
        assertThat(actual.getConnectTimeoutMillisecond(), is(4000));
        assertThat(actual.getReadTimeoutMillisecond(), is(2000));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        DingtalkConfiguration actual = DingtalkConfiguration.newBuilder("webhook", "keyword", "secret").build();
        assertThat(actual.getWebhook(), is("webhook"));
        assertThat(actual.getKeyword(), is("keyword"));
        assertThat(actual.getSecret(), is("secret"));
        assertThat(actual.getConnectTimeoutMillisecond(), is(3000));
        assertThat(actual.getReadTimeoutMillisecond(), is(5000));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyWebhook() {
        DingtalkConfiguration.newBuilder("", "keyword", "secret").build();
    }
}
