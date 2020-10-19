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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Job error handler for send error message via wechat.
 */
@Slf4j
public final class WechatJobErrorHandler implements JobErrorHandler<WechatConfiguration> {
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public WechatJobErrorHandler() {
        registerShutdownHook();
    }
    
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("WechatJobErrorHandler Shutdown-Hook") {

            @SneakyThrows
            @Override
            public void run() {
                log.info("Shutting down HTTP client...");
                httpclient.close();
            }
        });
    }
    
    @Override
    public void handleException(final String jobName, final WechatConfiguration config, final Throwable cause) {
        HttpPost httpPost = createHTTPPostMethod(jobName, cause, config);
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();
            if (HttpURLConnection.HTTP_OK == status) {
                JsonObject resp = GsonFactory.getGson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                if (!"0".equals(resp.get("errcode").getAsString())) {
                    log.info("An exception has occurred in Job '{}', But failed to send alert by wechat because of: {}", jobName, resp.get("errmsg").getAsString(), cause);
                } else {
                    log.info("An exception has occurred in Job '{}', Notification to wechat was successful.", jobName, cause);
                }
            } else {
                log.error("An exception has occurred in Job '{}', But failed to send alert by wechat because of: Unexpected response status: {}", jobName, status, cause);
            }
        } catch (final IOException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}', But failed to send alert by wechat because of", jobName, cause);
        }
    }
    
    private HttpPost createHTTPPostMethod(final String jobName, final Throwable cause, final WechatConfiguration config) {
        HttpPost result = new HttpPost(config.getWebhook());
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(config.getConnectTimeoutMillisecond()).setSocketTimeout(config.getReadTimeoutMillisecond()).build();
        result.setConfig(requestConfig);
        StringEntity entity = new StringEntity(getJsonParameter(getErrorMessage(jobName, cause)), StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType("application/json");
        result.setEntity(entity);
        return result;
    }
    
    private String getJsonParameter(final String message) {
        return GsonFactory.getGson().toJson(ImmutableMap.of("msgtype", "text", "text", Collections.singletonMap("content", message)));
    }
    
    private String getErrorMessage(final String jobName, final Throwable cause) {
        StringWriter stringWriter = new StringWriter();
        cause.printStackTrace(new PrintWriter(stringWriter, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, stringWriter.toString());
    }
    
    @Override
    public String getType() {
        return WechatType.TYPE;
    }
}
