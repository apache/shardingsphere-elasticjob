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

package org.apache.shardingsphere.elasticjob.lite.internal.util;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ParameterUtilsTest {
    
    @Test
    public void assertParseQueryString() {
        String queryString = "key1=foo&key2&key3=bar";
        Map<String, String> result = ParameterUtils.parseQuery(queryString);
        assertThat(result.get("key1"), is("foo"));
        assertThat(result.get("key2"), is(""));
        assertThat(result.get("key3"), is("bar"));
    }
    
    @Test
    public void assertParseEmptyString() {
        Map<String, String> result = ParameterUtils.parseQuery("");
        assertTrue(result.isEmpty());
    }
}
