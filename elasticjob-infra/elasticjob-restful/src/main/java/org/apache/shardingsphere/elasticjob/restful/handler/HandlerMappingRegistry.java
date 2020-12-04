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
     * @param httpRequest HTTP request
     * @return A MappingContext if matched, return null if mismatched.
     */
    public Optional<MappingContext<Handler>> getMappingContext(final HttpRequest httpRequest) {
        String uriWithoutQuery = httpRequest.uri().split("\\?")[0];
        return Optional.ofNullable(mappings.get(httpRequest.method())).map(urlPatternMap -> urlPatternMap.match(uriWithoutQuery));
    }
    
    /**
     * Add a Handler for a path pattern.
     *
     * @param method HTTP method
     * @param pathPattern path pattern
     * @param handler handler
     */
    public void addMapping(final HttpMethod method, final String pathPattern, final Handler handler) {
        UrlPatternMap<Handler> urlPatternMap = mappings.computeIfAbsent(method, httpMethod -> new RegexUrlPatternMap<>());
        urlPatternMap.put(pathPattern, handler);
    }
}
