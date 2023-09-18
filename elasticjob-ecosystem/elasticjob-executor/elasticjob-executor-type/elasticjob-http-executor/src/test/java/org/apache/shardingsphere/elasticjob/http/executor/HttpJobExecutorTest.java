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

package org.apache.shardingsphere.elasticjob.http.executor;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.http.executor.fixture.InternalController;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionException;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class HttpJobExecutorTest {
    
    private static final int PORT = 9876;
    
    private static final String HOST = "localhost";
    
    private static RestfulService restfulService;
    
    @Mock
    private ElasticJob elasticJob;
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private Properties properties;
    
    @Mock
    private ShardingContext shardingContext;
    
    private HttpJobExecutor jobExecutor;
    
    @BeforeAll
    public static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstances(new InternalController());
        restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
    }
    
    @BeforeEach
    public void setUp() {
        when(jobConfig.getProps()).thenReturn(properties);
        jobExecutor = new HttpJobExecutor();
    }
    
    @AfterAll
    public static void close() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
    
    @Test
    public void assertUrlEmpty() {
        assertThrows(JobConfigurationException.class, () -> {
            when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn("");
            jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
        });
    }
    
    @Test
    public void assertMethodEmpty() {
        assertThrows(JobConfigurationException.class, () -> {
            when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/getName"));
            when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("");
            jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
        });
    }
    
    @Test
    public void assertProcessWithoutSuccessCode() {
        when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/unknownMethod"));
        when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("GET");
        when(jobConfig.getProps().getProperty(HttpJobProperties.DATA_KEY)).thenReturn("");
        when(jobConfig.getProps().getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000")).thenReturn("4000");
        when(jobConfig.getProps().getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000")).thenReturn("5000");
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
    }
    
    @Test
    public void assertProcessWithGet() {
        when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/getName"));
        when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("GET");
        when(jobConfig.getProps().getProperty(HttpJobProperties.DATA_KEY)).thenReturn("");
        when(jobConfig.getProps().getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000")).thenReturn("4000");
        when(jobConfig.getProps().getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000")).thenReturn("5000");
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
    }
    
    @Test
    public void assertProcessHeader() {
        when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/getShardingContext"));
        when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("GET");
        when(jobConfig.getProps().getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000")).thenReturn("4000");
        when(jobConfig.getProps().getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000")).thenReturn("5000");
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
    }
    
    @Test
    public void assertProcessWithPost() {
        when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/updateName"));
        when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("POST");
        when(jobConfig.getProps().getProperty(HttpJobProperties.DATA_KEY)).thenReturn("name=elasticjob");
        when(jobConfig.getProps().getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000")).thenReturn("4000");
        when(jobConfig.getProps().getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000")).thenReturn("5000");
        when(jobConfig.getProps().getProperty(HttpJobProperties.CONTENT_TYPE_KEY)).thenReturn("application/x-www-form-urlencoded");
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
    }
    
    @Test
    public void assertProcessWithIOException() {
        assertThrows(JobExecutionException.class, () -> {
            when(jobConfig.getProps().getProperty(HttpJobProperties.URI_KEY)).thenReturn(getRequestUri("/postWithTimeout"));
            when(jobConfig.getProps().getProperty(HttpJobProperties.METHOD_KEY)).thenReturn("POST");
            when(jobConfig.getProps().getProperty(HttpJobProperties.DATA_KEY)).thenReturn("name=elasticjob");
            when(jobConfig.getProps().getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000")).thenReturn("1");
            when(jobConfig.getProps().getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000")).thenReturn("1");
            jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
        });
    }
    
    @Test
    public void assertGetType() {
        assertThat(jobExecutor.getType(), is("HTTP"));
    }
    
    private String getRequestUri(final String path) {
        return "http://" + HOST + ":" + PORT + path;
    }
}
