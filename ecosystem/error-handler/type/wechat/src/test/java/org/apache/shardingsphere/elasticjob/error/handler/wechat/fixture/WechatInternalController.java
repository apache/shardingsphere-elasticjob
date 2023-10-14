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

package org.apache.shardingsphere.elasticjob.error.handler.wechat.fixture;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;

public final class WechatInternalController implements RestfulController {
    
    private static final String KEY = "mocked_key";
    
    /**
     * Send wechat message.
     *
     * @param key access token
     * @return send Result
     */
    @SneakyThrows
    @Mapping(method = Http.POST, path = "/send")
    public String send(@Param(name = "key", source = ParamSource.QUERY) final String key) {
        if (!KEY.equals(key)) {
            return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 1, "errmsg", "token is invalid"));
        }
        return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 0, "errmsg", "ok"));
    }
}
