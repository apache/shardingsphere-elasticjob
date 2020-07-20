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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.job.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * Aop target Utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AopTargetUtils {
    
    /**
     * Get target object.
     * 
     * @param proxy proxy object
     * @return target object
     */
    public static Object getTarget(final Object proxy) {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getProxyTargetObject(proxy, "h");
        } else {
            return getProxyTargetObject(proxy, "CGLIB$CALLBACK_0");
        }
    }
    
    private static Object getProxyTargetObject(final Object proxy, final String proxyType) {
        Field h;
        try {
            h = proxy.getClass().getSuperclass().getDeclaredField(proxyType);
        } catch (final NoSuchFieldException ex) {
            return getProxyTargetObjectForCglibAndSpring4(proxy);
        }
        h.setAccessible(true);
        try {
            return getTargetObject(h.get(proxy));
        } catch (final IllegalAccessException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private static Object getProxyTargetObjectForCglibAndSpring4(final Object proxy) {
        Field h;
        try {
            h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            h.setAccessible(true);  
            Object dynamicAdvisedInterceptor = h.get(proxy);
            Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            throw new JobSystemException(ex);
        }
    }
    
    private static Object getTargetObject(final Object object) {
        try {
            Field advised = object.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            return ((AdvisedSupport) advised.get(object)).getTargetSource().getTarget();
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            throw new JobSystemException(ex);
        }
    }
}
