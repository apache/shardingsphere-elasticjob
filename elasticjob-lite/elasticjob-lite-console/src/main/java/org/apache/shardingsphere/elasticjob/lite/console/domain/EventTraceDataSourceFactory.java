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

package org.apache.shardingsphere.elasticjob.lite.console.domain;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Event trace data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTraceDataSourceFactory {
    
    private static final ConcurrentHashMap<HashCode, EventTraceDataSource> DATA_SOURCE_REGISTRY = new ConcurrentHashMap<>(); 
    
    /**
     * Create event trace data source.
     * 
     * @param driverClassName database driver class name
     * @param url database URL
     * @param username database username
     * @param password database password
     * @return event trace data source
     */
    public static EventTraceDataSource createEventTraceDataSource(final String driverClassName, final String url, final String username, final String password) {
        Hasher hasher = Hashing.sha256().newHasher().putString(driverClassName, Charsets.UTF_8).putString(url, Charsets.UTF_8);
        if (!Strings.isNullOrEmpty(username)) {
            hasher.putString(username, Charsets.UTF_8);
        }
        if (null != password) {
            hasher.putString(password, Charsets.UTF_8);
        }
        HashCode hashCode = hasher.hash();
        EventTraceDataSource result = DATA_SOURCE_REGISTRY.get(hashCode);
        if (null != result) {
            return result;
        }
        EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration = new EventTraceDataSourceConfiguration(driverClassName, url, username, password);
        result = new EventTraceDataSource(eventTraceDataSourceConfiguration);
        result.init();
        DATA_SOURCE_REGISTRY.put(hashCode, result);
        return result;
    }
}
