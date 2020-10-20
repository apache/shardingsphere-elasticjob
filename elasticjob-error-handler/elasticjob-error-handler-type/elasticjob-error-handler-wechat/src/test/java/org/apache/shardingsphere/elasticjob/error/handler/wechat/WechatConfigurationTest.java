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

package org.apache.shardingsphere.elasticjob.error.handler.wechat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WechatConfigurationTest {
    
    private static final String WEBHOOK = "webhook";
    
    private static final int CONNECT_TIMEOUT = 5000;
    
    private static final int CONNECT_TIMEOUT_DEFAULT_VALUE = 3000;
    
    private static final int READ_TIMEOUT = 5000;
    
    private static final int READ_TIMEOUT_DEFAULT_VALUE = 3000;
    
    private static final String EMPTY_STRING = "";
    
    @Test
    public void assertBuildAllProperties() {
        WechatConfiguration actual = WechatConfiguration.newBuilder(WEBHOOK)
                .connectTimeoutMillisecond(CONNECT_TIMEOUT)
                .readTimeoutMillisecond(READ_TIMEOUT)
                .build();
        assertThat(actual.getWebhook(), is(WEBHOOK));
        assertThat(actual.getConnectTimeoutMillisecond(), is(CONNECT_TIMEOUT));
        assertThat(actual.getReadTimeoutMillisecond(), is(READ_TIMEOUT));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        WechatConfiguration actual = WechatConfiguration.newBuilder(WEBHOOK).build();
        assertThat(actual.getWebhook(), is(WEBHOOK));
        assertThat(actual.getConnectTimeoutMillisecond(), is(CONNECT_TIMEOUT_DEFAULT_VALUE));
        assertThat(actual.getReadTimeoutMillisecond(), is(READ_TIMEOUT_DEFAULT_VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyWebhook() {
        WechatConfiguration.newBuilder(EMPTY_STRING).build();
    }
}
