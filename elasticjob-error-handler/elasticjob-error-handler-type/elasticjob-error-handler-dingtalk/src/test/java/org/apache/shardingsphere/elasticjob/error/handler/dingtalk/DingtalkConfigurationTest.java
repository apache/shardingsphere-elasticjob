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
public class DingtalkConfigurationTest {
    
    private static final String WEBHOOK = "webhook";
    
    private static final String KEYWORD = "keyword";
    
    private static final String SECRET = "secret";
    
    private static final int CONNECT_TIMEOUT = 4000;
    
    private static final int CONNECT_TIMEOUT_DEFAULT_VALUE = 3000;
    
    private static final int READ_TIMEOUT = 4000;
    
    private static final int READ_TIMEOUT_DEFAULT_VALUE = 5000;
    
    private static final String EMPTY_STRING = "";
    
    @Test
    public void assertBuildAllProperties() {
        DingtalkConfiguration actual = DingtalkConfiguration.newBuilder(WEBHOOK, KEYWORD, SECRET)
                .connectTimeoutMillisecond(CONNECT_TIMEOUT)
                .readTimeoutMillisecond(READ_TIMEOUT)
                .build();
        assertThat(actual.getWebhook(), is(WEBHOOK));
        assertThat(actual.getKeyword(), is(KEYWORD));
        assertThat(actual.getSecret(), is(SECRET));
        assertThat(actual.getConnectTimeoutMillisecond(), is(CONNECT_TIMEOUT));
        assertThat(actual.getReadTimeoutMillisecond(), is(CONNECT_TIMEOUT));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        DingtalkConfiguration actual = DingtalkConfiguration.newBuilder(WEBHOOK, KEYWORD, SECRET).build();
        assertThat(actual.getWebhook(), is(WEBHOOK));
        assertThat(actual.getKeyword(), is(KEYWORD));
        assertThat(actual.getSecret(), is(SECRET));
        assertThat(actual.getConnectTimeoutMillisecond(), is(CONNECT_TIMEOUT_DEFAULT_VALUE));
        assertThat(actual.getReadTimeoutMillisecond(), is(READ_TIMEOUT_DEFAULT_VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyWebhook() {
        DingtalkConfiguration.newBuilder(EMPTY_STRING, KEYWORD, SECRET).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyKeyword() {
        DingtalkConfiguration.newBuilder(WEBHOOK, EMPTY_STRING, SECRET).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptySecret() {
        DingtalkConfiguration.newBuilder(WEBHOOK, KEYWORD, EMPTY_STRING).build();
    }
}
