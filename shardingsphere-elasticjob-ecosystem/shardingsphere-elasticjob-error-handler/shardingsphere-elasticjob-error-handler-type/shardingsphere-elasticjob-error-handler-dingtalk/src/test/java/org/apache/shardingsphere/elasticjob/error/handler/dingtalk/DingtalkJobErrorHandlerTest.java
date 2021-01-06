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

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.error.handler.dingtalk.fixture.DingtalkInternalController;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class DingtalkJobErrorHandlerTest {
    
    private static final int PORT = 9875;
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    @Mock
    private Logger log;
    
    @BeforeClass
    public static void init() {
        NettyRestfulServiceConfiguration config = new NettyRestfulServiceConfiguration(PORT);
        config.setHost(HOST);
        config.addControllerInstances(new DingtalkInternalController());
        restfulService = new NettyRestfulService(config);
        restfulService.startup();
    }
    
    @AfterClass
    public static void close() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
    
    @Test
    public void assertHandleExceptionWithNotifySuccessful() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:9875/send?access_token=mocked_token"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).info("An exception has occurred in Job '{}', an dingtalk message been sent successful.", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongToken() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:9875/send?access_token=wrong_token"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send dingtalk because of: {}", "test_job", "token is not exist", cause);
    }
    
    @Test
    public void assertHandleExceptionWithUrlIsNotFound() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createConfigurationProperties("http://localhost:9875/404"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send dingtalk because of: unexpected http response status: {}", "test_job", 404, cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongUrl() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createNoSignJobConfigurationProperties("http://wrongUrl"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}', but failed to send dingtalk because of", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithNoSign() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler(createNoSignJobConfigurationProperties("http://localhost:9875/send?access_token=mocked_token"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).info("An exception has occurred in Job '{}', an dingtalk message been sent successful.", "test_job", cause);
    }
    
    private DingtalkJobErrorHandler getDingtalkJobErrorHandler(final Properties props) {
        return (DingtalkJobErrorHandler) JobErrorHandlerFactory.createHandler("DINGTALK", props).orElseThrow(() -> new JobConfigurationException("DINGTALK error handler not found."));
    }
    
    @SneakyThrows
    private void setStaticFieldValue(final DingtalkJobErrorHandler dingtalkJobErrorHandler) {
        Field field = dingtalkJobErrorHandler.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(dingtalkJobErrorHandler, log);
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
