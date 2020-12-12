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

package org.apache.shardingsphere.elasticjob.tracing.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for {@link TracingStorageConverter}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TracingStorageConverterFactory {
    
    private static final List<TracingStorageConverter<?>> CONVERTERS = new LinkedList<>();
    
    static {
        ServiceLoader.load(TracingStorageConverter.class).forEach(CONVERTERS::add);
    }
    
    /**
     * Find {@link TracingStorageConverter} for specific storage type.
     *
     * @param storageType storage type
     * @param <T>         storage type
     * @return instance of {@link TracingStorageConverter}
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<TracingStorageConverter<T>> findConverter(final Class<T> storageType) {
        return CONVERTERS.stream().filter(each -> each.storageType().isAssignableFrom(storageType)).map(each -> (TracingStorageConverter<T>) each).findFirst();
    }
}
