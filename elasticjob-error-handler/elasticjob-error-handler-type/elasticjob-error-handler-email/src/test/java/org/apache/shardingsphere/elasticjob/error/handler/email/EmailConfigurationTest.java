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

package org.apache.shardingsphere.elasticjob.error.handler.email;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EmailConfigurationTest {
    
    private static final String HOST = "192.168.0.8";
    
    private static final int PORT = 8080;
    
    private static final String USERNAME = "username";
    
    private static final String PASSWORD = "password";
    
    private static final String FROM = "from";
    
    private static final String TO = "to";
    
    private static final String SUBJECT = "subject";
    
    private static final String SUBJECT_DEFAULT_VALUE = "ElasticJob error message";
    
    private static final String CC = "cc";
    
    private static final String BCC = "bcc";
    
    private static final boolean USE_SSL = false;
    
    private static final boolean USE_SSL_DEFAULT_VALUE = true;
    
    private static final boolean DEBUG = true;
    
    private static final boolean DEBUG_DEFAULT_VALUE = false;
    
    private static final String EMPTY_STRING = "";
    
    @Test
    public void assertBuildAllProperties() {
        EmailConfiguration actual = EmailConfiguration.newBuilder(HOST, PORT, USERNAME, PASSWORD, FROM, TO)
                .useSsl(USE_SSL)
                .subject(SUBJECT)
                .cc(CC)
                .bcc(BCC)
                .debug(DEBUG)
                .build();
        assertThat(actual.getHost(), is(HOST));
        assertThat(actual.getPort(), is(PORT));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
        assertThat(actual.isUseSsl(), is(USE_SSL));
        assertThat(actual.getSubject(), is(SUBJECT));
        assertThat(actual.getFrom(), is(FROM));
        assertThat(actual.getTo(), is(TO));
        assertThat(actual.getCc(), is(CC));
        assertThat(actual.getBcc(), is(BCC));
        assertThat(actual.isDebug(), is(DEBUG));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        EmailConfiguration actual = EmailConfiguration.newBuilder(HOST, PORT, USERNAME, PASSWORD, FROM, TO)
                .build();
        assertThat(actual.getHost(), is(HOST));
        assertThat(actual.getPort(), is(PORT));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
        assertThat(actual.isUseSsl(), is(USE_SSL_DEFAULT_VALUE));
        assertThat(actual.getSubject(), is(SUBJECT_DEFAULT_VALUE));
        assertThat(actual.getFrom(), is(FROM));
        assertThat(actual.getTo(), is(TO));
        assertThat(actual.isDebug(), is(DEBUG_DEFAULT_VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyHost() {
        EmailConfiguration.newBuilder(EMPTY_STRING, PORT, USERNAME, PASSWORD, FROM, TO)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithInvalidPort() {
        EmailConfiguration.newBuilder(HOST, -1, USERNAME, PASSWORD, FROM, TO)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyUsername() {
        EmailConfiguration.newBuilder(HOST, PORT, EMPTY_STRING, PASSWORD, FROM, TO)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyPassword() {
        EmailConfiguration.newBuilder(HOST, PORT, USERNAME, EMPTY_STRING, FROM, TO)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyFROM() {
        EmailConfiguration.newBuilder(HOST, PORT, USERNAME, PASSWORD, EMPTY_STRING, TO)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyTo() {
        EmailConfiguration.newBuilder(HOST, PORT, USERNAME, PASSWORD, FROM, EMPTY_STRING)
                .build();
    }
}
