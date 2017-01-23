/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.spring.placeholder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理占位符的类.
 * 
 * @author zhangliang
 */
@Slf4j
public final class PlaceholderResolved {
    
    private final Map<String, PropertySourcesPlaceholderConfigurer> placeholderMap;
    
    public PlaceholderResolved(final ListableBeanFactory beanFactory) {
        placeholderMap = beanFactory.getBeansOfType(PropertySourcesPlaceholderConfigurer.class);
    }
    
    /**
     * 获取处理占位符后的文本值.
     * 
     * @param text 含有占位符的文本
     * @return 处理占位符后的文本值
     */
    public String getResolvePlaceholderText(final String text) {
        if (placeholderMap.isEmpty()) {
            return text;
        }
        IllegalArgumentException missingException = null;
        for (Entry<String, PropertySourcesPlaceholderConfigurer> entry : placeholderMap.entrySet()) {
            PropertySourcesPropertyResolver propertyResolver;
            try {
            	PropertySourcesPlaceholderConfigurer propertyConfigurer = entry.getValue();
                Method method = PropertySourcesPlaceholderConfigurer.class.getMethod("getAppliedPropertySources");
                PropertySources propertySources = (PropertySources)method.invoke(propertyConfigurer);
                propertyResolver = new PropertySourcesPropertyResolver(propertySources);
            } catch (final NoSuchMethodError ex) {
                try {
                    propertyResolver = getPropertyResolverBeforeSpring4(entry.getValue());
                } catch (final ReflectiveOperationException e) {
                    log.warn("Cannot get placeholder resolver.");
                    return text;
                }
            } catch (final Exception ex) {
				log.warn("Cannot access field[propertySources] from PropertySourcesPlaceholderConfigurer.");
                continue;
            } 
            try {
                return propertyResolver.resolveRequiredPlaceholders(text);
            } catch (final IllegalArgumentException ex) {
                missingException = ex;
            }
        }
        if (null == missingException) {
            return text;
        }
        throw missingException;
    }
    
    private PropertySourcesPropertyResolver getPropertyResolverBeforeSpring4(final PropertySourcesPlaceholderConfigurer placeholderConfigurer) 
    				throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException /*throws ReflectiveOperationException*/ {
    	Field field = PropertySourcesPlaceholderConfigurer.class.getDeclaredField("propertySources");
    	field.setAccessible(true);
    	PropertySources propertySources = (PropertySources)field.get(placeholderConfigurer);
        return new PropertySourcesPropertyResolver(propertySources);
    }
}
