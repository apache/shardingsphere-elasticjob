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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationConstants;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.exception.HttpClientException;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;

/**
 * Http utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpTestUtil {
    
    private static final AuthenticationService AUTH_SERVICE = new AuthenticationService();
    
    private static void setAuth(final HttpRequestBase httpRequestBase) {
        httpRequestBase.setHeader(AuthenticationConstants.HEADER_NAME, AUTH_SERVICE.getToken());
    }
    
    /**
     * Send post request.
     *
     * @param url url
     * @return http status code
     */
    public static int post(final String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            setAuth(httpPost);
            return httpClient.execute(httpPost).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new HttpClientException("send a post request for '%s' failed", e, url);
        }
    }
    
    /**
     * Send post request.
     *
     * @param url     url
     * @param content content
     * @return http status code
     */
    public static int post(final String url, final String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            setAuth(httpPost);
            StringEntity entity = new StringEntity(content, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            return httpClient.execute(httpPost).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new HttpClientException("send a post request for '%s' with parameter '%s' failed", e, url, content);
        }
    }
    
    /**
     * Send put request.
     *
     * @param url     url
     * @param content content
     * @return http status code
     */
    public static int put(final String url, final String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            setAuth(httpPut);
            StringEntity entity = new StringEntity(content, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);
            return httpClient.execute(httpPut).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new HttpClientException("send a put request for '%s' with parameter '%s' failed", e, url, content);
        }
    }
    
    /**
     * Send get request.
     *
     * @param url url
     * @return http result
     */
    public static String get(final String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            setAuth(httpGet);
            HttpEntity entity = httpClient.execute(httpGet).getEntity();
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            throw new HttpClientException("send a get request for '%s' failed", e, url);
        }
    }
    
    /**
     * Send get request.
     *
     * @param url     url
     * @param content content
     * @return http result
     */
    public static String get(final String url, final Map<String, String> content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : content.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            setAuth(httpGet);
            HttpEntity entity = httpClient.execute(httpGet).getEntity();
            return EntityUtils.toString(entity);
        } catch (IOException | URISyntaxException e) {
            throw new HttpClientException("send a get request for '%s' failed", e, url);
        }
    }
    
    /**
     * Send delete request.
     *
     * @param url url
     * @return http status code
     */
    public static int delete(final String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);
            setAuth(httpDelete);
            return httpClient.execute(httpDelete).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new HttpClientException("send a delete request for '%s' failed", e, url);
        }
    }
    
    /**
     * Send post request.
     *
     * @param url     url
     * @param content content
     * @return http response
     */
    public static CloseableHttpResponse unauthorizedPost(final String url, final Map<String, String> content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(GsonFactory.getGson().toJson(content), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            return httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new HttpClientException("send a post request for '%s' with parameter '%s' failed", e, url, content);
        }
    }
    
    /**
     * Send get request.
     *
     * @param url url
     * @return http status code
     */
    public static int unauthorizedGet(final String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            return httpClient.execute(httpGet).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new HttpClientException("send a get request for '%s' failed", e, url);
        }
    }
}
