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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.elasticjob.error.handler.dingtalk.fixture.DingtalkInternalController;
import org.apache.shardingsphere.elasticjob.spi.executor.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DingtalkJobErrorHandlerTest {
    
    private static final int PORT = InstanceSpec.getRandomPort();
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    private static List<LoggingEvent> appenderList;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeAll
    static void init() {
        NettyRestfulServiceConfiguration config = new NettyRestfulServiceConfiguration(PORT);
        config.setHost(HOST);
        config.addControllerInstances(new DingtalkInternalController());
        restfulService = new NettyRestfulService(config);
        restfulService.startup();
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DingtalkJobErrorHandler.class);
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("DingtalkJobErrorHandlerTestAppender");
        appenderList = appender.list;
    }
    
    @BeforeEach
    void setUp() {
        appenderList.clear();
    }
    
    @AfterAll
    static void close() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
    
    @Test
    void assertHandleExceptionWithNotifySuccessful() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:" + PORT + "/send?access_token=mocked_token"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.INFO));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job', an dingtalk message been sent successful."));
    }
    
    @Test
    void assertHandleExceptionWithWrongToken() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:" + PORT + "/send?access_token=wrong_token"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send dingtalk because of: token is not exist"));
    }
    
    @Test
    void assertHandleExceptionWithUrlIsNotFound() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:" + PORT + "/404"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send dingtalk because of: unexpected http response status: 404"));
    }
    
    @Test
    void assertHandleExceptionWithWrongUrl() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createNoSignJobConfigurationProperties("http://wrongUrl"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job', but failed to send dingtalk because of"));
    }
    
    @Test
    void assertHandleExceptionWithNoSign() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createNoSignJobConfigurationProperties("http://localhost:" + PORT + "/send?access_token=mocked_token"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.INFO));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job', an dingtalk message been sent successful."));
    }
    
    private DingtalkJobErrorHandler getDingtalkJobErrorHandler(final Properties props) {
        return (DingtalkJobErrorHandler) TypedSPILoader.getService(JobErrorHandler.class, "DINGTALK", props);
    }
    
    private Properties createConfigurationProperties(final String webhook) {
        Properties result = new Properties();
        result.setProperty(DingtalkPropertiesConstants.WEBHOOK, webhook);
        result.setProperty(DingtalkPropertiesConstants.KEYWORD, "mocked_keyword");
        result.setProperty(DingtalkPropertiesConstants.SECRET, "mocked_secret");
        result.setProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, "4000");
        result.setProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECONDS, "6000");
        return result;
    }
    
    private Properties createNoSignJobConfigurationProperties(final String webhook) {
        Properties result = new Properties();
        result.setProperty(DingtalkPropertiesConstants.WEBHOOK, webhook);
        result.setProperty(DingtalkPropertiesConstants.KEYWORD, "mocked_keyword");
        result.setProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, "4000");
        result.setProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECONDS, "6000");
        return result;
    }
}
