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

package org.apache.shardingsphere.elasticjob.error.handler.config;

import org.apache.shardingsphere.elasticjob.error.handler.fixture.EmailConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationBuilderTest {

    @Test
    public void assertBuildConfigByYaml() {
        EmailConfiguration emailConfiguration = ConfigurationBuilder.buildConfigByYaml("ejob-error-handle.email", EmailConfiguration.class);
        assertNotNull(emailConfiguration);
        assertThat(emailConfiguration.getHost(), equalTo("test.mail.com"));
        assertThat(emailConfiguration.getPort(), equalTo(123));
    }

}
