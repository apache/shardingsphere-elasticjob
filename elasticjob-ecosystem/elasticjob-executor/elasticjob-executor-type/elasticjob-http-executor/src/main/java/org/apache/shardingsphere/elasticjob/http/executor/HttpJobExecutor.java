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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.executor.item.impl.TypedJobItemExecutor;
import org.apache.shardingsphere.elasticjob.http.pojo.HttpParam;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionException;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * Http job executor.
 */
@Slf4j
public final class HttpJobExecutor implements TypedJobItemExecutor {
    
    @Override
    public void process(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        HttpParam httpParam = getHttpParam(jobConfig.getProps());
        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpParam.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpParam.getMethod());
            connection.setDoOutput(true);
            connection.setConnectTimeout(httpParam.getConnectTimeout());
            connection.setReadTimeout(httpParam.getReadTimeout());
            if (!Strings.isNullOrEmpty(httpParam.getContentType())) {
                connection.setRequestProperty("Content-Type", httpParam.getContentType());
            }
            connection.setRequestProperty(HttpJobProperties.SHARDING_CONTEXT_KEY, GsonFactory.getGson().toJson(shardingContext));
            connection.connect();
            String data = httpParam.getData();
            if (isWriteMethod(httpParam.getMethod()) && !Strings.isNullOrEmpty(data)) {
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                }
            }
            int code = connection.getResponseCode();
            InputStream resultInputStream;
            if (isRequestSucceed(code)) {
                resultInputStream = connection.getInputStream();
            } else {
                log.warn("HTTP job {} executed with response code {}", jobConfig.getJobName(), code);
                resultInputStream = connection.getErrorStream();
            }
            StringBuilder result = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultInputStream, StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = bufferedReader.readLine())) {
                    result.append(line);
                }
            }
            if (isRequestSucceed(code)) {
                log.debug("HTTP job execute result : {}", result.toString());
            } else {
                log.warn("HTTP job {} executed with response body {}", jobConfig.getJobName(), result.toString());
            }
        } catch (final IOException ex) {
            throw new JobExecutionException(ex);
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }
    
    private HttpParam getHttpParam(final Properties props) {
        String url = props.getProperty(HttpJobProperties.URI_KEY);
        if (Strings.isNullOrEmpty(url)) {
            throw new JobConfigurationException("Cannot find HTTP URL, job is not executed.");
        }
        String method = props.getProperty(HttpJobProperties.METHOD_KEY);
        if (Strings.isNullOrEmpty(method)) {
            throw new JobConfigurationException("Cannot find HTTP method, job is not executed.");
        }
        String data = props.getProperty(HttpJobProperties.DATA_KEY);
        int connectTimeout = Integer.parseInt(props.getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000"));
        int readTimeout = Integer.parseInt(props.getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000"));
        String contentType = props.getProperty(HttpJobProperties.CONTENT_TYPE_KEY);
        return new HttpParam(url, method, data, connectTimeout, readTimeout, contentType);
    }
    
    private boolean isWriteMethod(final String method) {
        return Arrays.asList("POST", "PUT", "DELETE").contains(method.toUpperCase());
    }
    
    private boolean isRequestSucceed(final int httpStatusCode) {
        return HttpURLConnection.HTTP_BAD_REQUEST > httpStatusCode;
    }
    
    @Override
    public String getType() {
        return "HTTP";
    }
}
