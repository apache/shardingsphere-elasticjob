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

package org.apache.shardingsphere.elasticjob.cloud.console.security;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.AuthConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;

/**
 * User authentication service.
 */
public final class AuthenticationService {
    
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    
    private final Base64 base64 = new Base64();
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getINSTANCE();
    
    /**
     * Check auth.
     *
     * @param authenticationInfo authentication info
     * @return check success or failure
     */
    public boolean check(final AuthenticationInfo authenticationInfo) {
        if (null == authenticationInfo || Strings.isNullOrEmpty(authenticationInfo.getUsername()) || Strings.isNullOrEmpty(authenticationInfo.getPassword())) {
            return false;
        }
        AuthConfiguration userAuthConfiguration = env.getUserAuthConfiguration();
        return userAuthConfiguration.getAuthUsername().equals(authenticationInfo.getUsername()) && userAuthConfiguration.getAuthPassword().equals(authenticationInfo.getPassword());
    }
    
    /**
     * Get user authentication token.
     *
     * @return authentication token
     */
    public String getToken() {
        return base64.encodeToString(gson.toJson(env.getUserAuthConfiguration()).getBytes());
    }
}
