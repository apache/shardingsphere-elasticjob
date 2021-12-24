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

package org.apache.shardingsphere.elasticjob.restful.deserializer.impl;

import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.restful.deserializer.RequestBodyDeserializer;

import java.nio.charset.StandardCharsets;

/**
 * Deserializer for <code>application/json</code>.
 */
public final class DefaultJsonRequestBodyDeserializer implements RequestBodyDeserializer {
    
    private final Gson gson = GsonFactory.getGson();
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public <T> T deserialize(final Class<T> targetType, final byte[] requestBodyBytes) {
        if (0 == requestBodyBytes.length) {
            return null;
        }
        return gson.fromJson(new String(requestBodyBytes, StandardCharsets.UTF_8), targetType);
    }
}
