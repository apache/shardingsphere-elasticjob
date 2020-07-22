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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.exception.HttpClientException;

/**
 * Http client utils.
 */
public class HttpClientUtils {
    
    /**
     * http get request.
     * @param url thr url
     * @return http result
     */
    public static HttpResult httpGet(final String url) {
        return httpGet(url, null, null, 3000L);
    }
    
    /**
     * http get request.
     * @param url           thr url
     * @param paramValues   the param values
     * @param encoding      the encoding
     * @param readTimeoutMs the read timeout
     * @return http result
     */
    public static HttpResult httpGet(final String url, final List<String> paramValues, final String encoding, final long readTimeoutMs) {
        HttpURLConnection conn = null;
        try {
            String encodedContent = encodingParams(paramValues, encoding);
            String urlWithParam = url + (null == encodedContent ? "" : ("?" + encodedContent));
            conn = (HttpURLConnection) new URL(urlWithParam).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout((int) readTimeoutMs);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.connect();
            String resp;
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                resp = IOUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(conn.getResponseCode(), resp);
        } catch (IOException ex) {
            throw new HttpClientException(ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    private static String encodingParams(final List<String> paramValues, final String encoding) throws UnsupportedEncodingException {
        if (null == paramValues) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext();) {
            sb.append(iter.next()).append("=");
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
    
    /**
     * http post request.
     * @param url thr url
     * @return http result
     */
    public static HttpResult httpPost(final String url) {
        return httpPost(url, null, null, 3000L);
    }
    
    /**
     * http post request.
     * @param url           thr url
     * @param paramValues   the param values
     * @param encoding      the encoding
     * @param readTimeoutMs the read timeout
     * @return http result
     */
    public static HttpResult httpPost(final String url, final List<String> paramValues, final String encoding, final long readTimeoutMs) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout((int) readTimeoutMs);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.getOutputStream().write(encodingParams(paramValues, encoding).getBytes(StandardCharsets.UTF_8));
            String resp;
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                resp = IOUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(conn.getResponseCode(), resp);
        } catch (IOException ex) {
            throw new HttpClientException(ex);
        } finally {
            if (null != conn) {
                conn.disconnect();
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
