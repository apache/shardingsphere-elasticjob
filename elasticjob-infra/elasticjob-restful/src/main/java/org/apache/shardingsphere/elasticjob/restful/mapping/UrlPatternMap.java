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

package org.apache.shardingsphere.elasticjob.restful.mapping;

/**
 * UrlPatternMap is used for path pattern storage and path matching.
 * {@link MappingContext} is an object holding path pattern and payload.
 *
 * @param <V> Type of payload
 */
public interface UrlPatternMap<V> {
    
    /**
     * Add a path pattern and value to UrlPatternMap.
     *
     * @param pathPattern Path pattern
     * @param value       Payload of the path pattern
     */
    void put(String pathPattern, V value);
    
    /**
     * Find a proper MappingContext for current path.
     *
     * @param path A path to match.
     * @return A MappingContext if the path matched a pattern. Return null if mismatched.
     */
    MappingContext<V> match(String path);
}
