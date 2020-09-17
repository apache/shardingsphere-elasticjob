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

package org.apache.shardingsphere.elasticjob.error.handler.impl.fixture;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;

@Slf4j
public final class DingtalkInternalController implements RestfulController {
    
    private static final String ACCESS_TOKEN = "42eead064e81ce81fc6af2c107fbe10a4339a3d40a7db8abf5b34d8261527a3f";
    
    /**
     * Send message.
     *
     * @param accessToken access token
     * @param timestamp timestamp
     * @param sign sign
     * @return send result
     */
    @Mapping(method = Http.POST, path = "/send")
    public String send(@Param(name = "access_token", source = ParamSource.QUERY) final String accessToken,
                       @Param(name = "timestamp", source = ParamSource.QUERY, required = false) final String timestamp,
                       @Param(name = "sign", source = ParamSource.QUERY, required = false) final String sign) {
        if (!ACCESS_TOKEN.equals(accessToken)) {
            return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 300001, "errmsg", "token is not exist"));
        }
        return GsonFactory.getGson().toJson(ImmutableMap.of("errcode", 0, "errmsg", "ok"));
    }
}
