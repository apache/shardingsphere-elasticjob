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
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.error.handler.dingtalk.configuration.DingtalkPropertiesConstants;
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
        config.addControllerInstance(new DingtalkInternalController());
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
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException(getJobConfiguration("http://localhost:9875/send?access_token=42eead064e81ce81fc6af2c107fbe10a4339a3d40a7db8abf5b34d8261527a3f"), cause);
        verify(log).info("An exception has occurred in Job '{}', Notification to Dingtalk was successful.", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongToken() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException(getJobConfiguration("http://localhost:9875/send?access_token=wrongToken"), cause);
        verify(log).info("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: {}", "test_job", "token is not exist", cause);
    }
    
    @Test
    public void assertHandleExceptionWithUrlIsNotFound() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException(getJobConfiguration("http://localhost:9875/404"), cause);
        verify(log).error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: Unexpected response status: {}", "test_job", 404, cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongUrl() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException(getNoSignJobConfiguration("http://wrongUrl"), cause);
        verify(log).error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithNoSign() {
        DingtalkJobErrorHandler actual = getDingtalkJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException(getNoSignJobConfiguration("http://localhost:9875/send?access_token=42eead064e81ce81fc6af2c107fbe10a4339a3d40a7db8abf5b34d8261527a3f"), cause);
        verify(log).info("An exception has occurred in Job '{}', Notification to Dingtalk was successful.", "test_job", cause);
    }
    
    private DingtalkJobErrorHandler getDingtalkJobErrorHandler() {
        return (DingtalkJobErrorHandler) JobErrorHandlerFactory.createHandler("DINGTALK").orElseThrow(() -> new JobConfigurationException("DINGTALK error handler not found."));
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
    
    private JobConfiguration getJobConfiguration(final String webhook) {
        return JobConfiguration.newBuilder("test_job", 3)
                .setProperty(DingtalkPropertiesConstants.WEBHOOK, webhook)
                .setProperty(DingtalkPropertiesConstants.KEYWORD, "keyword")
                .setProperty(DingtalkPropertiesConstants.SECRET, "SEC0b0a6b13b6823b95737dd83491c23adee5d8a7a649899a12217e038eddc84ff4")
                .setProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECOND, "4000")
                .setProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECOND, "6000")
                .build();
    }
    
    private JobConfiguration getNoSignJobConfiguration(final String webhook) {
        return JobConfiguration.newBuilder("test_job", 3)
                .setProperty(DingtalkPropertiesConstants.WEBHOOK, webhook)
                .setProperty(DingtalkPropertiesConstants.KEYWORD, "keyword")
                .setProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECOND, "4000")
                .setProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECOND, "6000")
                .build();
    }
}
