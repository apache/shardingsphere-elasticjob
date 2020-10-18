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

package org.apache.shardingsphere.elasticjob.infra.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.spi.exception.ServiceLoaderInstantiationException;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ElasticJob service loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticJobServiceLoader {
    
    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, TypedSPI>> TYPED_SERVICES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Class<?>>> TYPED_SERVICE_CLASSES = new ConcurrentHashMap<>();
    
    /**
     * Register typeSPI service.
     *
     * @param typedService specific service type
     * @param <T> type of service
     */
    public static <T> void registerTypedService(final Class<T> typedService) {
        if (!TypedSPI.class.isAssignableFrom(typedService)) {
            throw new IllegalArgumentException("Cannot register @" + typedService.getName() + "as a typed service, because its not a subClass of @" + TypedSPI.class.getName());
        }
        if (TYPED_SERVICES.containsKey(typedService)) {
            return;
        }
        ServiceLoader.load(typedService).forEach(each -> registerTypedServiceClass(typedService, (TypedSPI) each));
    }

    private static <T> void registerTypedServiceClass(final Class<T> typedService, final TypedSPI instance) {
        TYPED_SERVICES.computeIfAbsent(typedService, unused -> new ConcurrentHashMap<>()).putIfAbsent(instance.getType(), instance);
        TYPED_SERVICE_CLASSES.computeIfAbsent(typedService, unused -> new ConcurrentHashMap<>()).putIfAbsent(instance.getType(), instance.getClass());
    }

    /**
     * Get cached instance.
     *
     * @param typedServiceInterface typed service interface
     * @param type         specific service type
     * @param <T>          specific type of service
     * @return cached service instance
     */
    public static <T extends TypedSPI> Optional<T> getCachedTypedServiceInstance(final Class<T> typedServiceInterface, final String type) {
        T instance = TYPED_SERVICES.containsKey(typedServiceInterface) ? (T) TYPED_SERVICES.get(typedServiceInterface).get(type) : null;
        if (null == instance) {
            return Optional.empty();
        }
        return Optional.of(instance);
    }

    /**
     * New typed instance.
     *
     * @param typedServiceInterface typed service interface
     * @param type         specific service type
     * @param <T>          specific type of service
     * @return specific typed service instance
     */
    public static <T extends TypedSPI> Optional<T> newTypedServiceInstance(final Class<T> typedServiceInterface, final String type) {
        Class<?> instanceClass = TYPED_SERVICE_CLASSES.containsKey(typedServiceInterface) ? TYPED_SERVICE_CLASSES.get(typedServiceInterface).get(type) : null;
        if (null == instanceClass) {
            return Optional.empty();
        }
        return Optional.of((T) newServiceInstance(instanceClass));
    }

    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (final InstantiationException | NoSuchMethodException | IllegalAccessException ex) {
            throw new ServiceLoaderInstantiationException(clazz, ex);
        } catch (final InvocationTargetException ex) {
            throw new ServiceLoaderInstantiationException(clazz, ex.getCause());
        }
    }
}
