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
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.spi.exception.ServiceLoaderInstantiationException;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * ElasticJob service loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticJobServiceLoader {
    
    private static final ConcurrentMap<Class<?>, Collection<Class<?>>> SERVICES = new ConcurrentHashMap<>();
    
    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, TypedSPI>> TYPED_SERVICES = new ConcurrentHashMap<>();
    
    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Class<?>>> TYPED_SERVICE_CLASSES = new ConcurrentHashMap<>();
    
    /**
     * Register SPI service.
     *
     * @param service service type
     * @param <T> type of service
     */
    public static <T> void register(final Class<T> service) {
        if (SERVICES.containsKey(service)) {
            return;
        }
        ServiceLoader.load(service).forEach(each -> registerServiceClass(service, each));
    }
    
    private static <T> void registerServiceClass(final Class<T> service, final T instance) {
        SERVICES.computeIfAbsent(service, unused -> new LinkedHashSet<>()).add(instance.getClass());
    }
    
    /**
     * New service instances.
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        return SERVICES.containsKey(service) ? SERVICES.get(service).stream().map(each -> (T) newServiceInstance(each)).collect(Collectors.toList()) : Collections.emptyList();
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
    
    /**
     * Register typeSPI service.
     *
     * @param typedService specific service type
     * @param <T> type of service
     */
    public static <T> void registerTypedService(final Class<T> typedService) {
        if (!TypedSPI.class.isAssignableFrom(typedService)) {
            throw new IllegalArgumentException("Cannot register @" + typedService.getName() + "as a typed service, because its not a subClass of @" + typedService);
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
     * @param typedService typed service
     * @param type type
     * @param <T> class of service
     * @return cached service instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends TypedSPI> T getCachedInstance(final Class<T> typedService, final String type) {
        T result = TYPED_SERVICES.containsKey(typedService) ? (T) TYPED_SERVICES.get(typedService).get(type) : null;
        if (null == result) {
            throw new JobConfigurationException("Cannot find a cached typed service instance by the interface: @" + typedService.getName() + "and type: " + type);
        }
        return result;
    }
    
    /**
     * New typed instance.
     *
     * @param typedService typed service
     * @param type type
     * @param <T> class of service
     * @return new service instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends TypedSPI> T newTypedServiceInstance(final Class<T> typedService, final String type) {
        Class<?> instanceClass = TYPED_SERVICE_CLASSES.containsKey(typedService) ? TYPED_SERVICE_CLASSES.get(typedService).get(type) : null;
        if (null == instanceClass) {
            throw new JobConfigurationException("Cannot find a typed service class by the interface: @" + typedService.getName() + "and type: " + type);
        }
        return (T) newServiceInstance(instanceClass);
    }
}
