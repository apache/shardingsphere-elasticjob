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

package org.apache.shardingsphere.elasticjob.cloud.console.config.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.shardingsphere.elasticjob.restful.serializer.ResponseBodySerializer;

import java.nio.charset.StandardCharsets;

/**
 * Json response body serializer. Serialize object to json byte except String.
 */
public final class JsonResponseBodySerializer implements ResponseBodySerializer {
    
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public byte[] serialize(final Object responseBody) {
        if (responseBody instanceof String) {
            return ((String) responseBody).getBytes(StandardCharsets.UTF_8);
        }
        return gson.toJson(responseBody).getBytes(StandardCharsets.UTF_8);
    }
}
