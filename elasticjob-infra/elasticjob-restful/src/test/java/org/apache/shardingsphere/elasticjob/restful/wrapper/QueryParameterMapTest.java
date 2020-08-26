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

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class QueryParameterMapTest {
    
    @Test
    public void assertGetFirst() {
        QueryParameterMap queryParameterMap = new QueryParameterMap();
        queryParameterMap.add("name", "foo");
        assertThat(queryParameterMap.getFirst("name"), is("foo"));
        assertFalse(queryParameterMap.isEmpty());
    }
    
    @Test
    public void assertConvertToSingleValueMap() {
        Map<String, List<String>> queries = new LinkedHashMap<>(1 << 2);
        queries.put("foo", new LinkedList<>(Arrays.asList("first_foo", "second_foo")));
        queries.put("bar", new LinkedList<>(Arrays.asList("first_bar", "second_bar")));
        QueryParameterMap queryParameterMap = new QueryParameterMap(queries);
        Map<String, String> singleValueMap = queryParameterMap.toSingleValueMap();
        assertThat(singleValueMap.get("foo"), is("first_foo"));
        assertThat(singleValueMap.get("bar"), is("first_bar"));
    }
    
    @Test
    public void assertGetEntrySet() {
        QueryParameterMap queryParameterMap = new QueryParameterMap();
        queryParameterMap.put("foo", new LinkedList<>(Arrays.asList("first_foo", "second_foo")));
        queryParameterMap.put("bar", new LinkedList<>(Arrays.asList("first_bar", "second_bar")));
        Set<Map.Entry<String, List<String>>> entrySet = queryParameterMap.entrySet();
        assertThat(entrySet.size(), is(2));
    }
}
