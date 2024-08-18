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

package org.apache.shardingsphere.elasticjob.kernel.tracing.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.spi.tracing.storage.TracingStorageConfigurationConverter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Optional;

/**
 * Factory for {@link TracingStorageConfigurationConverter}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TracingStorageConverterFactory {
    
    /**
     * Find {@link TracingStorageConfigurationConverter} for specific storage type.
     *
     * @param storageType storage type
     * @param <T> storage type
     * @return instance of {@link TracingStorageConfigurationConverter}
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<TracingStorageConfigurationConverter<T>> findConverter(final Class<T> storageType) {
        return ShardingSphereServiceLoader.getServiceInstances(TracingStorageConfigurationConverter.class).stream()
                .filter(each -> each.storageType().isAssignableFrom(storageType)).map(each -> (TracingStorageConfigurationConverter<T>) each).findFirst();
    }
}
