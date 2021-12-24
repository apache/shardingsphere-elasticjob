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

package org.apache.shardingsphere.elasticjob.error.handler.dingtalk.fixture;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

public final class DingtalkInternalController implements RestfulController {
    
    private static final String ACCESS_TOKEN = "mocked_token";
    
    private static final String KEYWORD = "mocked_keyword";
    
    private static final String SECRET = "mocked_secret";
    
    /**
     * Send Dingtalk message.
     *
     * @param accessToken access token
     * @param timestamp timestamp
     * @param sign sign
     * @param body body
     * @return send Result
     */
    @SneakyThrows
    @Mapping(method = Http.POST, path = "/send")
    public String send(@Param(name = "access_token", source = ParamSource.QUERY) final String accessToken,
                       @Param(name = "timestamp", source = ParamSource.QUERY, required = false) final Long timestamp,
                       @Param(name = "sign", source = ParamSource.QUERY, required = false) final String sign,
                       @RequestBody final Map<String, Object> body) {
        if (!ACCESS_TOKEN.equals(accessToken)) {
            return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 300001, "errmsg", "token is not exist"));
        }
        String content = Map.class.cast(body.get("text")).get("content").toString();
        if (!content.startsWith(KEYWORD)) {
            return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 310000, "errmsg", "keywords not in content, more: [https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq]"));
        }
        if (!Strings.isNullOrEmpty(sign)) {
            Preconditions.checkNotNull(timestamp);
            String checkSign = sign(timestamp);
            if (!sign.equals(checkSign)) {
                return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 310000, "errmsg", "sign not match, more: [https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq]"));
            }
        }
        return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 0, "errmsg", "ok"));
    }
    
    private String sign(final Long timestamp) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + SECRET;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(signData), StandardCharsets.UTF_8.name());
    }
}
