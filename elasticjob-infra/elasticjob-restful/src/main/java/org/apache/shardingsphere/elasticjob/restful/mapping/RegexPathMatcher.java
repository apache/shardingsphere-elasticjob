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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implemented {@link PathMatcher} by regular expression.
 */
public final class RegexPathMatcher implements PathMatcher {
    
    private static final String PATH_SEPARATOR = "/";
    
    private static final Pattern PATH_PATTERN = Pattern.compile("^/(([^/{}]+|\\{[^/{}]+})(/([^/{}]+|\\{[^/{}]+}))*/?)?$");
    
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(?<template>[^/]+)}");
    
    private static final String TEMPLATE_REGEX = "(?<${template}>[^/]+)";
    
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, String> captureVariables(final String pathPattern, final String path) {
        Pattern compiled = getCompiledPattern(pathPattern);
        String pathWithoutQuery = trimUriQuery(path);
        Matcher matcher = compiled.matcher(pathWithoutQuery);
        if (!matcher.matches() || 0 == matcher.groupCount()) {
            return Collections.emptyMap();
        }
        Map<String, String> variables = new LinkedHashMap<>();
        for (String variableName : extractTemplateNames(pathPattern)) {
            variables.put(variableName, matcher.group(variableName));
        }
        return Collections.unmodifiableMap(variables);
    }
    
    @Override
    public boolean matches(final String pathPattern, final String path) {
        return getCompiledPattern(pathPattern).matcher(trimUriQuery(path)).matches();
    }
    
    @Override
    public boolean isValidPathPattern(final String pathPattern) {
        return PATH_PATTERN.matcher(pathPattern).matches();
    }
    
    private Pattern getCompiledPattern(final String pathPattern) {
        String regexPattern = convertToRegexPattern(pathPattern);
        patternCache.computeIfAbsent(regexPattern, Pattern::compile);
        return patternCache.get(regexPattern);
    }
    
    private String convertToRegexPattern(final String pathPattern) {
        return TEMPLATE_PATTERN.matcher(pathPattern).replaceAll(TEMPLATE_REGEX);
    }
    
    private List<String> extractTemplateNames(final String pathPattern) {
        String[] pathFragments = pathPattern.split(PATH_SEPARATOR);
        List<String> result = new ArrayList<>();
        for (String fragment : pathFragments) {
            int start = fragment.indexOf('{');
            int end = fragment.lastIndexOf('}');
            if (-1 != start && -1 != end) {
                result.add(fragment.substring(start + 1, end));
            }
        }
        return result;
    }
    
    private String trimUriQuery(final String uri) {
        int index = uri.indexOf('?');
        if (-1 != index) {
            return uri.substring(0, index);
        }
        return uri;
    }
}
