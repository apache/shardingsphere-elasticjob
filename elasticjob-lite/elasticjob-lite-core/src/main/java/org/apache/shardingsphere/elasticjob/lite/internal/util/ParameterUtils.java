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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parameter utils.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterUtils {
    
    /**
     * Parse string like <code>key1=value1&amp;key2=value2</code> to {@link Map}.
     *
     * @param query parameter string
     * @return map
     */
    public static Map<String, String> parseQuery(final String query) {
        if (Strings.isNullOrEmpty(query)) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&")).map(String::trim)
                .filter(pair -> !pair.isEmpty())
                .map(parameter -> parameter.split("="))
                .collect(Collectors.toMap(pair -> pair[0], pair -> 1 < pair.length ? pair[1] : ""));
    }
}
