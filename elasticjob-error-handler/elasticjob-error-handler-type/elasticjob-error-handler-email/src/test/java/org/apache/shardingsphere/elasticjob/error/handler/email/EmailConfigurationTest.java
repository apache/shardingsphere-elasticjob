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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class EmailConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        EmailConfiguration actual = EmailConfiguration.newBuilder("smtp.xxx.com", 25, "username", "password", "from@xx.xx", "to@xx.xx")
                .useSsl(false).subject("subject").cc("cc@xx.xx").bcc("bcc@xx.xx").debug(true).build();
        assertThat(actual.getHost(), is("smtp.xxx.com"));
        assertThat(actual.getPort(), is(25));
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getPassword(), is("password"));
        assertFalse(actual.isUseSsl());
        assertThat(actual.getSubject(), is("subject"));
        assertThat(actual.getFrom(), is("from@xx.xx"));
        assertThat(actual.getTo(), is("to@xx.xx"));
        assertThat(actual.getCc(), is("cc@xx.xx"));
        assertThat(actual.getBcc(), is("bcc@xx.xx"));
        assertTrue(actual.isDebug());
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        EmailConfiguration actual = EmailConfiguration.newBuilder("smtp.xxx.com", 25, "username", "password", "from@xx.xx", "to@xx.xx").build();
        assertThat(actual.getHost(), is("smtp.xxx.com"));
        assertThat(actual.getPort(), is(25));
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getPassword(), is("password"));
        assertTrue(actual.isUseSsl());
        assertThat(actual.getSubject(), is("ElasticJob error message"));
        assertThat(actual.getFrom(), is("from@xx.xx"));
        assertThat(actual.getTo(), is("to@xx.xx"));
        assertFalse(actual.isDebug());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyHost() {
        EmailConfiguration.newBuilder("", 25, "username", "password", "from@xx.xx", "to@xx.xx").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithInvalidPort() {
        EmailConfiguration.newBuilder("smtp.xxx.com", -1, "username", "password", "from@xx.xx", "to@xx.xx").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyUsername() {
        EmailConfiguration.newBuilder("smtp.xxx.com", 25, "", "password", "from@xx.xx", "to@xx.xx").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyPassword() {
        EmailConfiguration.newBuilder("smtp.xxx.com", 25, "username", "", "from@xx.xx", "to@xx.xx").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyFrom() {
        EmailConfiguration.newBuilder("smtp.xxx.com", 25, "username", "password", "", "to@xx.xx").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyTo() {
        EmailConfiguration.newBuilder("smtp.xxx.com", 25, "username", "password", "from@xx.xx", "").build();
    }
}
