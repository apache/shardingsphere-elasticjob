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
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ServiceLoader;

import org.apache.shardingsphere.elasticjob.error.handler.wechat.fixture.WechatInternalController;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class WechatJobErrorHandlerTest {
    
    private static final int PORT = 9876;
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    @Mock
    private Logger log;
    
    @BeforeClass
    public static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstance(new WechatInternalController());
        restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
    }
    
    @Test
    public void assertHandleExceptionWithNotifySuccessful() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}', Notification to wechat was successful.", "test_job", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongToken() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler();
        actual.setWechatConfiguration(new WechatConfiguration(getHost() + "/send?key=wrongToken", 3000, 500));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}', But failed to send alert by wechat because of: {}", "test_job", "token is invalid", cause);
    }
    
    @Test
    public void assertHandleExceptionWithWrongUrl() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler();
        actual.setWechatConfiguration(new WechatConfiguration(getHost() + "/404?access_token=wrongToken", 3000, 500));
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("An exception has occurred in Job '{}', But failed to send alert by wechat because of: Unexpected response status: {}", "test_job", 404, cause);
    }
    
    @Test
    public void assertGetType() {
        WechatJobErrorHandler actual = getWechatJobErrorHandler();
        assertThat(actual.getType(), is("WECHAT"));
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
    
    private String getHost() {
        return String.format("http://%s:%s", HOST, PORT);
    }
    
    private WechatJobErrorHandler getWechatJobErrorHandler() {
        for (JobErrorHandler jobErrorHandler : ServiceLoader.load(JobErrorHandler.class)) {
            if (null != jobErrorHandler && jobErrorHandler instanceof WechatJobErrorHandler) {
                return (WechatJobErrorHandler) jobErrorHandler;
            }
        }
        return new WechatJobErrorHandler();
    }
    
    @AfterClass
    public static void close() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
}
