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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.exception.HttpClientException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * Http client utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientUtils {
    
    /**
     * Http get request.
     *
     * @param url url
     * @return http result
     */
    public static HttpResult httpGet(final String url) {
        return httpGet(url, null, null, 3000L);
    }
    
    /**
     * Http get request.
     *
     * @param url url
     * @param paramValues param values
     * @param encoding encoding
     * @param readTimeoutMilliseconds read timeout milliseconds
     * @return http result
     */
    public static HttpResult httpGet(final String url, final List<String> paramValues, final String encoding, final long readTimeoutMilliseconds) {
        HttpURLConnection connection = null;
        try {
            String encodedContent = encodingParams(paramValues, encoding);
            String urlWithParam = url + (null == encodedContent ? "" : ("?" + encodedContent));
            connection = (HttpURLConnection) new URL(urlWithParam).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout((int) readTimeoutMilliseconds);
            connection.setReadTimeout((int) readTimeoutMilliseconds);
            connection.connect();
            String response;
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                response = IOUtils.toString(connection.getInputStream(), encoding);
            } else {
                response = IOUtils.toString(connection.getErrorStream(), encoding);
            }
            return new HttpResult(connection.getResponseCode(), response);
        } catch (final IOException ex) {
            throw new HttpClientException(ex);
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }
    
    private static String encodingParams(final List<String> paramValues, final String encoding) throws UnsupportedEncodingException {
        if (null == paramValues || 0 == paramValues.size()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext();) {
            stringBuilder.append(iter.next()).append("=");
            stringBuilder.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                stringBuilder.append("&");
            }
        }
        return stringBuilder.toString();
    }
    
    /**
     * Http post request.
     *
     * @param url url
     * @return http result
     */
    public static HttpResult httpPost(final String url) {
        return httpPost(url, null, null, 3000L);
    }
    
    /**
     * Http post request.
     *
     * @param url url
     * @param paramValues param values
     * @param encoding encoding
     * @param readTimeoutMilliseconds read timeout milliseconds
     * @return http result
     */
    public static HttpResult httpPost(final String url, final List<String> paramValues, final String encoding, final long readTimeoutMilliseconds) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout((int) readTimeoutMilliseconds);
            connection.setReadTimeout((int) readTimeoutMilliseconds);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.getOutputStream().write(encodingParams(paramValues, encoding).getBytes(StandardCharsets.UTF_8));
            String response;
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                response = IOUtils.toString(connection.getInputStream(), encoding);
            } else {
                response = IOUtils.toString(connection.getErrorStream(), encoding);
            }
            return new HttpResult(connection.getResponseCode(), response);
        } catch (final IOException ex) {
            throw new HttpClientException(ex);
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }
    
    @Getter
    @Setter
    public static class HttpResult {
        
        private final int code;
        
        private final String content;
        
        public HttpResult(final int code, final String content) {
            this.code = code;
            this.content = content;
        }
    }
}
