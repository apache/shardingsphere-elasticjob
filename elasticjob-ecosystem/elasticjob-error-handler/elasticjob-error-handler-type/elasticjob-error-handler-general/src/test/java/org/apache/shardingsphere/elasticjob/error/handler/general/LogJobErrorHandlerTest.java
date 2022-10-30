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

package org.apache.shardingsphere.elasticjob.error.handler.general;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class LogJobErrorHandlerTest {
    
    private static List<LoggingEvent> appenderList;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeClass
    public static void setupLogger() {
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LogJobErrorHandler.class);
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("LogJobErrorHandlerTestAppender");
        appenderList = appender.list;
    }
    
    @Before
    public void setUp() {
        appenderList.clear();
    }
    
    @Test
    public void assertHandleException() {
        LogJobErrorHandler actual = (LogJobErrorHandler) JobErrorHandlerFactory.createHandler("LOG", new Properties()).orElseThrow(() -> new JobConfigurationException("LOG error handler not found."));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Job 'test_job' exception occur in job processing"));
        actual.close();
    }
}
