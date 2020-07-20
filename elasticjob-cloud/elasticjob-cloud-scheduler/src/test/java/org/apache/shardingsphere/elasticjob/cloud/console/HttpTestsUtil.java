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

package org.apache.shardingsphere.elasticjob.cloud.console;


import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public final class HttpTestsUtil {
    
    /**
     * send post request
     * @param url the url
     * @return the http status code
     * @throws Exception exception when error
     */
    public static int post(final String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            return httpClient.execute(httpPost).getStatusLine().getStatusCode();
        }
    }
    
    /**
     * send post request
     * @param url     the url
     * @param content the content
     * @return the http status code
     * @throws Exception exception when error
     */
    public static int post(final String url, final String content) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(content, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            return httpClient.execute(httpPost).getStatusLine().getStatusCode();
        }
    }
    
    /**
     * send put request
     * @param url     the url
     * @param content the content
     * @return the http status code
     * @throws Exception exception when error
     */
    public static int put(final String url, final String content) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            StringEntity entity = new StringEntity(content, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);
            return httpClient.execute(httpPut).getStatusLine().getStatusCode();
        }
    }
    
    /**
     * Send get request.
     * @param url the url
     * @return the http response
     * @throws Exception exception when error
     */
    public static String get(final String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            HttpEntity entity = httpClient.execute(httpGet).getEntity();
            return EntityUtils.toString(entity);
        }
    }
    
    /**
     * send get request
     * @param url     the url
     * @param content the content
     * @return the http response
     * @throws Exception exception when error
     */
    public static String get(final String url, final Map<String, String> content) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : content.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            HttpEntity entity = httpClient.execute(httpGet).getEntity();
            return EntityUtils.toString(entity);
        }
    }
    
    /**
     * send delete request
     * @param url the url
     * @return the http status code
     * @throws Exception exception when error
     */
    public static int delete(final String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);
            return httpClient.execute(httpDelete).getStatusLine().getStatusCode();
        }
    }
}
