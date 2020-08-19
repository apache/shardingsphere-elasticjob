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

import com.google.common.base.Preconditions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Implemented {@link UrlPatternMap} by regular expression.
 *
 * @param <V> Type of payload
 */
public final class RegexUrlPatternMap<V> implements UrlPatternMap<V> {
    
    private static final String PATH_SEPARATOR = "/";
    
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("(?<=/)\\{(?<template>[^/]+)}");
    
    private final Map<String, MappingContext<V>> map = new LinkedHashMap<>();
    
    private final PathMatcher pathMatcher = new RegexPathMatcher();
    
    @Override
    public void put(final String pathPattern, final V value) {
        Objects.requireNonNull(pathPattern, "Path pattern must be not null.");
        Preconditions.checkArgument(pathMatcher.isValidPathPattern(pathPattern), "Path pattern [%s] invalid.", pathPattern);
        String unified = unifyPattern(pathPattern);
        MappingContext<V> mappingContext = new DefaultMappingContext<>(pathPattern, value);
        if (map.containsKey(unified)) {
            throw new IllegalArgumentException(String.format("Duplicate pattern [%s]", unified));
        }
        map.put(unified, mappingContext);
    }
    
    @Override
    public MappingContext<V> match(final String path) {
        List<MappingContext<V>> hits = new ArrayList<>();
        for (Map.Entry<String, MappingContext<V>> entry : map.entrySet()) {
            final String pattern = entry.getKey();
            if (pattern.equals(path)) {
                return entry.getValue();
            }
            if (pathMatcher.matches(pattern, path)) {
                hits.add(entry.getValue());
            }
        }
        if (hits.isEmpty()) {
            return null;
        }
        if (1 < hits.size()) {
            hits.sort(new MappingComparator().reversed());
        }
        return hits.get(0);
    }
    
    private String unifyPattern(final String pattern) {
        return TEMPLATE_PATTERN.matcher(pattern).replaceAll("[^/]+");
    }
    
    static class MappingComparator implements Comparator<MappingContext<?>> {
        
        @Override
        public int compare(final MappingContext<?> o1, final MappingContext<?> o2) {
            String[] s1 = o1.pattern().split(PATH_SEPARATOR);
            String[] s2 = o2.pattern().split(PATH_SEPARATOR);
            int len = Math.min(s1.length, s2.length);
            for (int i = 0; i < len; i++) {
                if (isTemplate(s1[i]) && !isTemplate(s2[i])) {
                    return -1;
                }
                if (!isTemplate(s1[i]) && isTemplate(s2[i])) {
                    return 1;
                }
            }
            throw new AmbiguousPathPatternException(MessageFormat.format("Ambiguous path pattern: [{0}], [{1}].", o1.pattern(), o2.pattern()));
        }
        
        private static boolean isTemplate(final String fragment) {
            return fragment.startsWith("{") && fragment.endsWith("}");
        }
    }
}
