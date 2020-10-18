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

package org.apache.shardingsphere.elasticjob.restful.wrapper;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrap a multi-value map. Helps handle method receiving all query parameters.
 */
public final class QueryParameterMap extends AbstractMap<String, List<String>> {
    
    private final Map<String, List<String>> queryMap;
    
    public QueryParameterMap() {
        queryMap = new LinkedHashMap<>();
    }
    
    public QueryParameterMap(final Map<String, List<String>> map) {
        queryMap = new LinkedHashMap<>(map);
    }
    
    /**
     * Get values by parameter name.
     *
     * @param parameterName parameter name
     * @return values
     */
    public List<String> get(final String parameterName) {
        return queryMap.get(parameterName);
    }
    
    /**
     * Get the first from values.
     *
     * @param parameterName parameter name
     * @return first value
     */
    public String getFirst(final String parameterName) {
        String firstValue = null;
        List<String> values = queryMap.get(parameterName);
        if (values != null && !values.isEmpty()) {
            firstValue = values.get(0);
        }
        return firstValue;
    }
    
    @Override
    public boolean isEmpty() {
        return queryMap.isEmpty();
    }
    
    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return queryMap.entrySet();
    }
    
    /**
     * Add value.
     *
     * @param parameterName parameter name
     * @param value value
     */
    public void add(final String parameterName, final String value) {
        List<String> values = queryMap.get(parameterName);
        if (null == values) {
            values = new LinkedList<>();
        }
        values.add(value);
        put(parameterName, values);
    }
    
    @Override
    public List<String> put(final String parameterName, final List<String> value) {
        return queryMap.put(parameterName, value);
    }
    
    /**
     * Convert to a single value map, abandon values except the first of each parameter.
     *
     * @return single value map
     */
    public Map<String, String> toSingleValueMap() {
        return queryMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
    }
}
