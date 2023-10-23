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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.error.handler.wechat.fixture.WechatInternalController;
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

class WechatJobErrorHandlerTest {
    
    private static final int PORT = 9872;
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    private static List<LoggingEvent> appenderList;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeAll
    static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstances(new WechatInternalController());
        restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WechatJobErrorHandler.class);
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("WechatJobErrorHandlerTestAppender");
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
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/send?key=mocked_key"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.INFO));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job', an wechat message has been sent successful."));
    }
    
    @Test
    void assertHandleExceptionWithWrongToken() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/send?key=wrong_key"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send wechat because of: token is invalid"));
    }
    
    @Test
    void assertHandleExceptionWithWrongUrl() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://wrongUrl"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send wechat because of"));
    }
    
    @Test
    void assertHandleExceptionWithUrlIsNotFound() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/404"));
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send wechat because of: unexpected http response status: 404"));
    }
    
    private WechatJobErrorHandler getWechatJobErrorHandler(final Properties props) {
        return (WechatJobErrorHandler) TypedSPILoader.getService(JobErrorHandler.class, "WECHAT", props);
    }
    
    private Properties createConfigurationProperties(final String webhook) {
        Properties result = new Properties();
        result.setProperty(WechatPropertiesConstants.WEBHOOK, webhook);
        result.setProperty(WechatPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, "1000");
        result.setProperty(WechatPropertiesConstants.READ_TIMEOUT_MILLISECONDS, "2000");
        return result;
    }
}
