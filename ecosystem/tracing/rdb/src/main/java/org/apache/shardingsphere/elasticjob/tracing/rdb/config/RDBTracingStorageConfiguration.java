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

package org.apache.shardingsphere.elasticjob.tracing.rdb.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.spi.tracing.storage.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.datasource.DataSourceRegistry;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.datasource.JDBCParameterDecorator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * RDB tracing storage configuration.
 */
@RequiredArgsConstructor
@Getter
public final class RDBTracingStorageConfiguration implements TracingStorageConfiguration<DataSource> {
    
    private static final String GETTER_PREFIX = "get";
    
    private static final String SETTER_PREFIX = "set";
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    private static final Collection<String> SKIPPED_PROPERTY_NAMES;
    
    static {
        GENERAL_CLASS_TYPE = Sets.newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class, Collection.class, List.class);
        SKIPPED_PROPERTY_NAMES = Sets.newHashSet("loginTimeout");
    }
    
    private final String dataSourceClassName;
    
    private final Map<String, Object> props = new LinkedHashMap<>();
    
    /**
     * Get data source configuration.
     *
     * @param dataSource data source
     * @return data source configuration
     */
    public static RDBTracingStorageConfiguration getDataSourceConfiguration(final DataSource dataSource) {
        RDBTracingStorageConfiguration result = new RDBTracingStorageConfiguration(dataSource.getClass().getName());
        result.props.putAll(findAllGetterProperties(dataSource));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Map<String, Object> findAllGetterProperties(final Object target) {
        Collection<Method> allGetterMethods = findAllGetterMethods(target.getClass());
        Map<String, Object> result = new LinkedHashMap<>(allGetterMethods.size(), 1);
        for (Method each : allGetterMethods) {
            String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, each.getName().substring(GETTER_PREFIX.length()));
            if (GENERAL_CLASS_TYPE.contains(each.getReturnType()) && !SKIPPED_PROPERTY_NAMES.contains(propertyName)) {
                Optional.ofNullable(each.invoke(target)).ifPresent(propertyValue -> result.put(propertyName, propertyValue));
            }
        }
        return result;
    }
    
    private static Collection<Method> findAllGetterMethods(final Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Collection<Method> result = new HashSet<>(methods.length);
        for (Method each : methods) {
            if (each.getName().startsWith(GETTER_PREFIX) && 0 == each.getParameterTypes().length) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Create data source.
     *
     * @return data source
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ReflectiveOperationException.class)
    public DataSource createDataSource() {
        DataSource result = (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
        Method[] methods = result.getClass().getMethods();
        for (Entry<String, Object> entry : props.entrySet()) {
            if (SKIPPED_PROPERTY_NAMES.contains(entry.getKey())) {
                continue;
            }
            Optional<Method> setterMethod = findSetterMethod(methods, entry.getKey());
            if (setterMethod.isPresent()) {
                setterMethod.get().invoke(result, entry.getValue());
            }
        }
        Optional<JDBCParameterDecorator> decorator = TypedSPILoader.findService(JDBCParameterDecorator.class, result.getClass());
        return decorator.isPresent() ? decorator.get().decorate(result) : result;
    }
    
    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = Joiner.on("").join(SETTER_PREFIX, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property));
        for (Method each : methods) {
            if (each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public DataSource getStorage() {
        return DataSourceRegistry.getInstance().getDataSource(this);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((RDBTracingStorageConfiguration) obj);
    }
    
    private boolean equalsByProperties(final RDBTracingStorageConfiguration dataSourceConfig) {
        return dataSourceClassName.equals(dataSourceConfig.dataSourceClassName) && props.equals(dataSourceConfig.props);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(dataSourceClassName, props);
    }
}
