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

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.error.handler.wechat.fixture.WechatInternalController;
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
public final class WechatJobErrorHandlerTest {
    
    private static final int PORT = 9872;
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    @Mock
    private Logger log;
    
    @BeforeClass
    public static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstances(new WechatInternalController());
        restfulService = new NettyRestfulService(configuration);
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
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/send?key=mocked_key"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).info("An exception has occurred in Job '{}', an wechat message has been sent successful.", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongToken() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/send?key=wrong_key"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send wechat because of: {}", "test_job", "token is invalid", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongUrl() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://wrongUrl"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send wechat because of", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithUrlIsNotFound() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler(createConfigurationProperties("http://localhost:9872/404"));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send wechat because of: unexpected http response status: {}", "test_job", 404, cause);
    }
    
    private WechatJobErrorHandler getWechatJobErrorHandler(final Properties props) {
        return (WechatJobErrorHandler) JobErrorHandlerFactory.createHandler("WECHAT", props).orElseThrow(() -> new JobConfigurationException("WECHAT error handler not found."));
    }
    
    @SneakyThrows
    private void setStaticFieldValue(final WechatJobErrorHandler wechatJobErrorHandler) {
        Field field = wechatJobErrorHandler.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(wechatJobErrorHandler, log);
    }
    
    private Properties createConfigurationProperties(final String webhook) {
        Properties result = new Properties();
        result.setProperty(WechatPropertiesConstants.WEBHOOK, webhook);
        result.setProperty(WechatPropertiesConstants.CONNECT_TIMEOUT_MILLISECONDS, "1000");
        result.setProperty(WechatPropertiesConstants.READ_TIMEOUT_MILLISECONDS, "2000");
        return result;
    }
}
