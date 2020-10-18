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

import java.util.Map;

/**
 * PathMatcher is a supporting tool for HTTP request dispatching.
 * <p>
 * Used by {@link UrlPatternMap}, {@link org.apache.shardingsphere.elasticjob.restful.pipeline.HandlerParameterDecoder}
 * for template variables extracting, path pattern validating, pattern matching.
 * </p>
 *
 * @see RegexPathMatcher
 */
public interface PathMatcher {
    
    /**
     * Capture actual values of placeholder.
     * The format of Path pattern likes <code>/app/{jobName}/{status}</code>.
     *
     * @param pathPattern path pattern contains templates
     * @param path actual path
     * @return map from template name to actual value
     */
    Map<String, String> captureVariables(String pathPattern, String path);
    
    /**
     * Check if the path pattern matches the given path.
     *
     * @param pathPattern path pattern
     * @param path the path to check
     * @return true if matched, or else false
     */
    boolean matches(String pathPattern, String path);
    
    /**
     * Check if the given string is a valid path pattern.
     *
     * @param pathPattern path pattern to check
     * @return true if valid, or else false
     */
    boolean isValidPathPattern(String pathPattern);
}
