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
import org.apache.shardingsphere.elasticjob.http.pojo.HttpParam;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionException;
import org.apache.shardingsphere.elasticjob.kernel.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.spi.executor.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.spi.executor.param.ShardingContext;
import org.apache.shardingsphere.elasticjob.spi.executor.type.TypedJobItemExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Http job executor.
 */
@Slf4j
public final class HttpJobExecutor implements TypedJobItemExecutor {
    
    @Override
    public void process(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobRuntimeService jobRuntimeService, final ShardingContext shardingContext) {
        HttpParam httpParam = new HttpParam(jobConfig.getProps());
        HttpURLConnection connection = null;
        try {
            connection = getHttpURLConnection(httpParam, shardingContext);
            connection.connect();
            String data = httpParam.getData();
            if (httpParam.isWriteMethod() && !Strings.isNullOrEmpty(data)) {
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                }
            }
            int responseCode = connection.getResponseCode();
            StringBuilder result = new StringBuilder();
            try (
                    InputStream inputStream = getConnectionInputStream(jobConfig.getJobName(), connection, responseCode);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = bufferedReader.readLine())) {
                    result.append(line);
                }
            }
            if (isRequestSucceed(responseCode)) {
                log.debug("HTTP job execute result : {}", result);
            } else {
                log.warn("HTTP job {} executed with response body {}", jobConfig.getJobName(), result);
            }
        } catch (final IOException ex) {
            throw new JobExecutionException(ex);
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }
    
    private HttpURLConnection getHttpURLConnection(final HttpParam httpParam, final ShardingContext shardingContext) throws IOException {
        URL url = new URL(httpParam.getUrl());
        HttpURLConnection result = (HttpURLConnection) url.openConnection();
        result.setRequestMethod(httpParam.getMethod());
        result.setDoOutput(true);
        result.setConnectTimeout(httpParam.getConnectTimeoutMilliseconds());
        result.setReadTimeout(httpParam.getReadTimeoutMilliseconds());
        if (!Strings.isNullOrEmpty(httpParam.getContentType())) {
            result.setRequestProperty("Content-Type", httpParam.getContentType());
        }
        result.setRequestProperty(HttpJobProperties.SHARDING_CONTEXT_KEY, GsonFactory.getGson().toJson(shardingContext));
        return result;
    }
    
    private InputStream getConnectionInputStream(final String jobName, final HttpURLConnection connection, final int code) throws IOException {
        if (isRequestSucceed(code)) {
            return connection.getInputStream();
        }
        log.warn("HTTP job {} executed with response code {}", jobName, code);
        return connection.getErrorStream();
    }
    
    private boolean isRequestSucceed(final int httpStatusCode) {
        return HttpURLConnection.HTTP_BAD_REQUEST > httpStatusCode;
    }
    
    @Override
    public String getType() {
        return "HTTP";
    }
}
