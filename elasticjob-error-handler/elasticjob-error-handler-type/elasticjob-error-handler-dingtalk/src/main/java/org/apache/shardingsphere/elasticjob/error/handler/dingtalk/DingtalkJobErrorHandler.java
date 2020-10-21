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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
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
import java.util.Properties;

/**
 * Job error handler for send error message via dingtalk.
 */
@Slf4j
public final class DingtalkJobErrorHandler implements JobErrorHandler {
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public DingtalkJobErrorHandler() {
        registerShutdownHook();
    }
    
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("DingtalkJobErrorHandler Shutdown-Hook") {
            
            @SneakyThrows
            @Override
            public void run() {
                log.info("Shutting down HTTP client...");
                httpclient.close();
            }
        });
    }
    
    @Override
    public void handleException(final String jobName, final Properties props, final Throwable cause) {
        HttpPost httpPost = createHTTPPostMethod(jobName, cause, createConfiguration(props));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();
            if (HttpURLConnection.HTTP_OK == status) {
                JsonObject responseMessage = GsonFactory.getGson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                if (!"0".equals(responseMessage.get("errcode").getAsString())) {
                    log.info("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: {}", jobName, responseMessage.get("errmsg").getAsString(), cause);
                } else {
                    log.info("An exception has occurred in Job '{}', Notification to Dingtalk was successful.", jobName, cause);
                }
            } else {
                log.error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of: Unexpected response status: {}", jobName, status, cause);
            }
        } catch (final IOException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}', But failed to send alert by Dingtalk because of", jobName, cause);
        }
    }
    
    private DingtalkConfiguration createConfiguration(final Properties props) {
        String webhook = props.getProperty(DingtalkPropertiesConstants.WEBHOOK);
        String keyword = props.getProperty(DingtalkPropertiesConstants.KEYWORD);
        String secret = props.getProperty(DingtalkPropertiesConstants.SECRET);
        int connectTimeoutMillisecond = Integer.parseInt(props.getProperty(DingtalkPropertiesConstants.CONNECT_TIMEOUT_MILLISECOND, DingtalkPropertiesConstants.DEFAULT_CONNECT_TIMEOUT_MILLISECOND));
        int readTimeoutMillisecond = Integer.parseInt(props.getProperty(DingtalkPropertiesConstants.READ_TIMEOUT_MILLISECOND, DingtalkPropertiesConstants.DEFAULT_READ_TIMEOUT_MILLISECOND));
        return new DingtalkConfiguration(webhook, keyword, secret, connectTimeoutMillisecond, readTimeoutMillisecond);
    }
    
    private HttpPost createHTTPPostMethod(final String jobName, final Throwable cause, final DingtalkConfiguration config) {
        HttpPost result = new HttpPost(getURL(config));
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(config.getConnectTimeoutMillisecond()).setSocketTimeout(config.getReadTimeoutMillisecond()).build();
        result.setConfig(requestConfig);
        StringEntity entity = new StringEntity(getJsonParameter(getErrorMessage(jobName, config, cause)), StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType("application/json");
        result.setEntity(entity);
        return result;
    }
    
    private String getURL(final DingtalkConfiguration config) {
        return Strings.isNullOrEmpty(config.getSecret()) ? config.getWebhook() : getSignedURL(config);
    }
    
    private String getSignedURL(final DingtalkConfiguration config) {
        long timestamp = System.currentTimeMillis();
        return String.format("%s&timestamp=%s&sign=%s", config.getWebhook(), timestamp, generateSignature(timestamp, config.getSecret()));
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, UnsupportedEncodingException.class, InvalidKeyException.class})
    private String generateSignature(final long timestamp, final String secret) {
        String algorithmName = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithmName);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithmName));
        byte[] signData = mac.doFinal((timestamp + "\n" + secret).getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), StandardCharsets.UTF_8.name());
    }
    
    private String getJsonParameter(final String message) {
        return GsonFactory.getGson().toJson(ImmutableMap.of("msgtype", "text", "text", Collections.singletonMap("content", message)));
    }
    
    private String getErrorMessage(final String jobName, final DingtalkConfiguration config, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        String result = String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
        if (!Strings.isNullOrEmpty(config.getKeyword())) {
            result = config.getKeyword().concat(result);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return DingtalkType.TYPE;
    }
}
