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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.SchedulerDriver;

/**
 * 发布任务作业调度管理器工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProducerManagerFactory {
    
    private static volatile ProducerManager instance;
    
    /**
     * 获取发布任务作业调度管理器.
     *
     * @param schedulerDriver Mesos控制器
     * @param regCenter 注册中心
     * @return 发布任务作业调度管理器对象
     */
    public static ProducerManager getInstance(final SchedulerDriver schedulerDriver, final CoordinatorRegistryCenter regCenter) {
        if (null == instance) {
            synchronized (ProducerManager.class) {
                if (null == instance) {
                    instance = new ProducerManager(schedulerDriver, regCenter);
                }
            }
        }
        return instance;
    }
}
