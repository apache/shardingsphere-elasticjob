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
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

/**
 * Job error handler for wechat error message.
 */
@Slf4j
public final class WechatJobErrorHandler implements JobErrorHandler {
    
    private static final String CONFIG_PREFIX = "wechat";
    
    private static final String ERROR_HANDLER_CONFIG = "conf/error-handler-wechat.yaml";
    
    @Setter
    private WechatConfiguration wechatConfiguration;
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public WechatJobErrorHandler() {
        registerShutdownHook();
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        if (null == wechatConfiguration) {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_HANDLER_CONFIG);
            wechatConfiguration = YamlEngine.unmarshal(CONFIG_PREFIX, inputStream, WechatConfiguration.class);
        }
        HttpPost httpPost = new HttpPost(getUrl());
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(wechatConfiguration.getConnectTimeoutOrDefault())
                .setSocketTimeout(wechatConfiguration.getReadTimeoutOrDefault())
                .build();
        httpPost.setConfig(requestConfig);
        StringEntity entity = new StringEntity(getParamJson(getMsg(jobName, cause)), StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();
            if (HttpURLConnection.HTTP_OK == status) {
                JsonObject resp = GsonFactory.getGson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                if (!"0".equals(resp.get("errcode").getAsString())) {
                    log.error("An exception has occurred in Job '{}', But failed to send alert by wechat because of: {}", jobName, resp.get("errmsg").getAsString(), cause);
                } else {
                    log.error("An exception has occurred in Job '{}', Notification to wechat was successful.", jobName, cause);
                }
            } else {
                log.error("An exception has occurred in Job '{}', But failed to send alert by wechat because of: Unexpected response status: {}", jobName, status, cause);
            }
        } catch (IOException ex) {
            log.error("An exception has occurred in Job '{}', But failed to send alert by wechat because of", jobName, ex);
        }
    }
    
    private String getParamJson(final String msg) {
        return GsonFactory.getGson().toJson(ImmutableMap.of(
                "msgtype", "text", "text", Collections.singletonMap("content", msg)
        ));
    }
    
    private String getMsg(final String jobName, final Throwable cause) {
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, sw.toString());
    }
    
    private String getUrl() {
        String webhook = wechatConfiguration.getWebhook();
        if (Objects.isNull(webhook)) {
            throw new RuntimeException("Please specify the wechat webhook address");
        }
        return webhook;
    }
    
    @Override
    public String getType() {
        return "WECHAT";
    }
    
    private void registerShutdownHook() {
        Thread t = new Thread("WechatJobErrorHandler Shutdown-Hook") {
            @SneakyThrows
            @Override
            public void run() {
                log.info("Shutting down httpclient...");
                httpclient.close();
            }
        };
        Runtime.getRuntime().addShutdownHook(t);
    }
}
