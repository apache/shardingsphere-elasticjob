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

package org.apache.shardingsphere.elasticjob.infra.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import java.lang.reflect.Type;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonFactory {
    
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    
    private static volatile Gson gson = GSON_BUILDER.create();
    
    private static final JsonParser JSON_PARSER = new JsonParser();
    
    /**
     * Register type adapter.
     *
     * @param type Gson type
     * @param typeAdapter Gson type adapter
     */
    public static synchronized void registerTypeAdapter(final Type type, final TypeAdapter typeAdapter) {
        GSON_BUILDER.registerTypeAdapter(type, typeAdapter);
        gson = GSON_BUILDER.create();
    }
    
    /**
     * Get gson instance.
     *
     * @return gson instance
     */
    public static Gson getGson() {
        return gson;
    }
    
    /**
     * Get json parser.
     *
     * @return json parser instance
     */
    public static JsonParser getJsonParser() {
        return JSON_PARSER;
    }
}
