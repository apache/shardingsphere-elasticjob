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

package org.apache.shardingsphere.elasticjob.restful.handler;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.shardingsphere.elasticjob.restful.mapping.MappingContext;
import org.apache.shardingsphere.elasticjob.restful.mapping.RegexUrlPatternMap;
import org.apache.shardingsphere.elasticjob.restful.mapping.UrlPatternMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * HandlerMappingRegistry stores mappings of handlers.
 * Search a proper {@link MappingContext} by HTTP method and request URI.
 */
public final class HandlerMappingRegistry {
    
    private final Map<HttpMethod, UrlPatternMap<Handler>> mappings = new HashMap<>();
    
    /**
     * Get a MappingContext with Handler for the request.
     *
     * @param httpRequest Http request
     * @return A MappingContext if matched. Return null if mismatched.
     */
    public MappingContext<Handler> getMappingContext(final HttpRequest httpRequest) {
        UrlPatternMap<Handler> urlPatternMap = mappings.get(httpRequest.method());
        String uriWithoutQuery = httpRequest.uri().split("\\?")[0];
        return Optional
                .ofNullable(urlPatternMap.match(uriWithoutQuery))
                .orElse(null);
    }
    
    /**
     * Add a Handler for a path pattern.
     *
     * @param method      HTTP method
     * @param pathPattern Path pattern
     * @param handler     Handler
     */
    public void addMapping(final HttpMethod method, final String pathPattern, final Handler handler) {
        mappings.computeIfAbsent(method, httpMethod -> new RegexUrlPatternMap<>());
        UrlPatternMap<Handler> urlPatternMap = mappings.get(method);
        urlPatternMap.put(pathPattern, handler);
    }
}
