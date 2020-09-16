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

package org.apache.shardingsphere.elasticjob.error.handler.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.error.handler.config.DingtalkConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.env.DingtalkEnvironment;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionException;
import org.apache.shardingsphere.elasticjob.infra.handler.error.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Job error handler for dingtalk error message.
 */
@Slf4j
public final class DingtalkJobErrorHandler implements JobErrorHandler {
    
    private DingtalkConfiguration dingtalkConfiguration;
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        if (dingtalkConfiguration == null) {
            dingtalkConfiguration = DingtalkEnvironment.getINSTANCE().getDingtalkConfiguration();
        }
        HttpURLConnection connection = null;
        try {
            URL url = getUrl();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(dingtalkConfiguration.getConnectTimeout());
            connection.setReadTimeout(dingtalkConfiguration.getReadTimeout());
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            String msg = getMsg(jobName, cause);
            String paramJson = getParamJson(msg);
            outputStream.write(paramJson.getBytes(StandardCharsets.UTF_8));
            int code = connection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == code) {
                InputStream resultInputStream = connection.getInputStream();
                StringBuilder result = new StringBuilder();
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultInputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while (null != (line = bufferedReader.readLine())) {
                        result.append(line);
                    }
                }
                JsonObject resp = GsonFactory.getGson().fromJson(result.toString(), JsonObject.class);
                if (!"0".equals(resp.get("errcode").getAsString())) {
                    log.error("Job '{}' exception occur in job processing, But the notification Dingtalk failure, error is : {}", jobName, resp.get("errmsg").getAsString(), cause);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new JobExecutionException(ex);
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }
    
    private String getParamJson(final String msg) {
        Map<String, Object> param = Maps.newLinkedHashMap();
        param.put("msgtype", "text");
        param.put("text", Collections.singletonMap("content", msg));
        return GsonFactory.getGson().toJson(param);
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
    
    private URL getUrl() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, MalformedURLException {
        if (Strings.isNullOrEmpty(dingtalkConfiguration.getSecret())) {
            return new URL(dingtalkConfiguration.getWebhook());
        } else {
            return new URL(getSignUrl());
        }
    }
    
    private String getSignUrl() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Long timestamp = System.currentTimeMillis();
        return String.format("%s&timestamp=%s&sign=%s", dingtalkConfiguration.getWebhook(), timestamp, sign(timestamp));
    }
    
    private String sign(final Long timestamp) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + dingtalkConfiguration.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(dingtalkConfiguration.getSecret().getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), "UTF-8");
    }
    
    @Override
    public String getType() {
        return "DINGTALK";
    }
}
