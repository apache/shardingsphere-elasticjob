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

package org.apache.shardingsphere.elasticjob.cloud.console.controller;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.shardingsphere.elasticjob.cloud.console.AbstractCloudControllerTest;
import org.apache.shardingsphere.elasticjob.cloud.console.HttpTestUtil;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationConstants;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationService;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CloudLoginTest extends AbstractCloudControllerTest {
    
    @Test
    public void assertLoginSuccess() throws IOException {
        Map<String, String> authInfo = new HashMap<>();
        authInfo.put("username", "root");
        authInfo.put("password", "pwd");
        CloseableHttpResponse actual = HttpTestUtil.unauthorizedPost("http://127.0.0.1:19000/api/login", authInfo);
        assertThat(actual.getStatusLine().getStatusCode(), is(200));
        AuthenticationService authenticationService = new AuthenticationService();
        String entity = EntityUtils.toString(actual.getEntity());
        String token = GsonFactory.getGson().fromJson(entity, JsonObject.class).get(AuthenticationConstants.HEADER_NAME).getAsString();
        assertThat(token, is(authenticationService.getToken()));
    }
    
    @Test
    public void assertLoginFail() {
        Map<String, String> authInfo = new HashMap<>();
        authInfo.put("username", "root");
        authInfo.put("password", "");
        CloseableHttpResponse actual = HttpTestUtil.unauthorizedPost("http://127.0.0.1:19000/api/login", authInfo);
        assertThat(actual.getStatusLine().getStatusCode(), is(401));
    }
    
    @Test
    public void assertUnauthorized() {
        assertThat(HttpTestUtil.unauthorizedGet("http://127.0.0.1:19000/api/unauthorized"), is(401));
    }
}
