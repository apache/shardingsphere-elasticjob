/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.framework;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 框架通用工具.
 * 
 * @author gaohongtao
 */
@Slf4j
@NoArgsConstructor
public class Frameworks {
    
    /**
     * 创建新框架.
     * 
     * @param registryCenter 注册中心
     * @return 框架对象
     */
    public static AbstractFramework newFramework(final CoordinatorRegistryCenter registryCenter) {
        MesosSchedulerFramework mesosSchedulerFramework = new MesosSchedulerFramework(registryCenter);
        mesosSchedulerFramework.setDelegate(new UserServiceFramework(registryCenter));
        HAFramework result = HAFramework.getInstance(registryCenter);
        result.setDelegate(mesosSchedulerFramework);
        return result;
    }
    
    static void invoke(final String message, final Invokable invokable) throws Exception {
        log.info("Elastic job: {}", message);
        try {
            invokable.invoke();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            log.error("Elastic job: {} error", message, ex);
            throw ex;
        }
    }
    
    static void safetyInvoke(final String message, final Invokable invokable) {
        try {
            invoke(message, invokable);
            //CHECKSTYLE:OFF
        } catch (final Exception ignored) {
        }
        //CHECKSTYLE:ON
    }
    
    interface Invokable {
        void invoke() throws Exception;
    }
}
