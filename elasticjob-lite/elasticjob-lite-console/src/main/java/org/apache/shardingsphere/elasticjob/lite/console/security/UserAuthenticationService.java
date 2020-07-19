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

package org.apache.shardingsphere.elasticjob.lite.console.security;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User authentication service.
 **/
@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class UserAuthenticationService {
    
    private String rootUsername;
    
    private String rootPassword;
    
    private String guestUsername;
    
    private String guestPassword;
    
    /**
     * Check user.
     *
     * @param authorization authorization
     * @param method method
     * @return authorization result
     */
    public AuthenticationResult checkUser(final String authorization, final String method) {
        Map<String, String> authorizationMap = parseAuthorizationMap(authorization);
        String username = authorizationMap.get("username");
        String realm = authorizationMap.get("realm");
        String uri = authorizationMap.get("uri");
        String nonce = authorizationMap.get("nonce");
        String nc = authorizationMap.get("nc");
        String cnonce = authorizationMap.get("cnonce");
        String qop = authorizationMap.get("qop");
        String response = authorizationMap.get("response");
        String password;
        AuthenticationResult authenticationResult;
        if (rootUsername.equals(username)) {
            password = rootPassword;
            authenticationResult = new AuthenticationResult(true, false);
        } else if (guestUsername.equals(username)) {
            password = guestPassword;
            authenticationResult = new AuthenticationResult(true, true);
        } else {
            return new AuthenticationResult(false, false);
        }
        String hash1 = Hashing.md5().hashBytes((username + ":" + realm + ":" + password).getBytes()).toString();
        String hash2 = Hashing.md5().hashBytes((method + ":" + uri).getBytes()).toString();
        String exceptResponse = Hashing.md5().hashBytes((hash1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + hash2).getBytes()).toString();
        
        if (StringUtils.equals(response, exceptResponse)) {
            return authenticationResult;
        }
        return new AuthenticationResult(false, false);
    }

    private static Map<String, String> parseAuthorizationMap(final String authority) {
        if (StringUtils.isBlank(authority)) {
            return Collections.emptyMap();
        }
        String authorityWithoutPrefix = authority.substring(authority.indexOf(" ") + 1);
        List<String> keyValueList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(authorityWithoutPrefix);
        Map<String, String> result = new HashMap<>();
        for (String keyValue : keyValueList) {
            int index = keyValue.indexOf("=");
            if (-1 != index) {
                String key = keyValue.substring(0, index);
                String value = keyValue.substring(index + 1).replaceAll("\"", "").trim();
                result.put(key, value);
            }
        }
        return result;
    }
}
