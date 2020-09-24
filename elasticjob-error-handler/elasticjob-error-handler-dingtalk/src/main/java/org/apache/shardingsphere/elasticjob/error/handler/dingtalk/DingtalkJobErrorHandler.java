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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import lombok.Setter;
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
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;

/**
 * Job error handler for dingtalk error message.
 */
@Slf4j
public final class DingtalkJobErrorHandler implements JobErrorHandler {
    
    private static final String CONFIG_PREFIX = "dingtalk";
    
    private static final String ERROR_HANDLER_CONFIG = "conf/error-handler-dingtalk.yaml";
    
    @Setter
    private DingtalkConfiguration dingtalkConfiguration;
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public DingtalkJobErrorHandler() {
        registerShutdownHook();
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        if (null == dingtalkConfiguration) {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_HANDLER_CONFIG);
            dingtalkConfiguration = YamlEngine.unmarshal(CONFIG_PREFIX, inputStream, DingtalkConfiguration.class);
        }
        HttpPost httpPost = new HttpPost(getUrl());
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(dingtalkConfiguration.getConnectTimeout())
                .setSocketTimeout(dingtalkConfiguration.getReadTimeout()).build();
        httpPost.setConfig(requestConfig);
        String paramJson = getParamJson(getMsg(jobName, cause));
        StringEntity entity = new StringEntity(paramJson, StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();
            if (HttpURLConnection.HTTP_OK == status) {
                JsonObject resp = GsonFactory.getGson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                if (!"0".equals(resp.get("errcode").getAsString())) {
                    log.error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: {}", jobName, resp.get("errmsg").getAsString(), cause);
                } else {
                    log.error("An exception has occurred in Job '{}', Notification to Dingtalk was successful.", jobName, cause);
                }
            } else {
                log.error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: Unexpected response status: {}", jobName, status, cause);
            }
        } catch (IOException ex) {
            log.error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of", jobName, ex);
        }
    }
    
    private String getParamJson(final String msg) {
        return GsonFactory.getGson().toJson(ImmutableMap.of("msgtype", "text", "text", Collections.singletonMap("content", msg)));
    }
    
    private String getMsg(final String jobName, final Throwable cause) {
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw, true));
        String msg = String.format("Job '%s' exception occur in job processing, caused by %s", jobName, sw.toString());
        if (!Strings.isNullOrEmpty(dingtalkConfiguration.getKeyword())) {
            msg = dingtalkConfiguration.getKeyword().concat(msg);
        }
        return msg;
    }
    
    private String getUrl() {
        if (Strings.isNullOrEmpty(dingtalkConfiguration.getSecret())) {
            return dingtalkConfiguration.getWebhook();
        } else {
            return getSignUrl();
        }
    }
    
    private String getSignUrl() {
        try {
            Long timestamp = System.currentTimeMillis();
            return String.format("%s&timestamp=%s&sign=%s", dingtalkConfiguration.getWebhook(), timestamp, sign(timestamp));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException ex) {
            throw new JobConfigurationException(ex);
        }
    }
    
    private String sign(final Long timestamp) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + dingtalkConfiguration.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(dingtalkConfiguration.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), StandardCharsets.UTF_8.name());
    }
    
    @Override
    public String getType() {
        return "DINGTALK";
    }
    
    private void registerShutdownHook() {
        Thread t = new Thread("DingtalkJobErrorHandler Shutdown-Hook") {
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
