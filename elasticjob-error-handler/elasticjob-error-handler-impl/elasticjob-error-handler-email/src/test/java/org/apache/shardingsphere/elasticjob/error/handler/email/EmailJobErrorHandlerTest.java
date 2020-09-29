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

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class EmailJobErrorHandlerTest {
    
    @Test
    public void assertHandleExceptionWithYAMLConfiguration() {
        EmailJobErrorHandler emailJobErrorHandler = new EmailJobErrorHandler();
        emailJobErrorHandler.handleException(getJobConfiguration(), new RuntimeException("test exception"));
    }
    
    private JobConfiguration getJobConfiguration() {
        return JobConfiguration.newBuilder("test_job", 3)
                .setProperty("email.host", "xxx")
                .setProperty("email.port", "1234")
                .setProperty("email.username", "username")
                .setProperty("email.password", "password")
                .setProperty("email.protocol", "protocol")
                .setProperty("email.useSsl", "true")
                .setProperty("email.form", "xxx")
                .setProperty("email.to", "xxx")
                .setProperty("email.cc", "xxx")
                .setProperty("email.bcc", "xxx")
                .setProperty("email.debug", "true")
                .build();
    }
}
