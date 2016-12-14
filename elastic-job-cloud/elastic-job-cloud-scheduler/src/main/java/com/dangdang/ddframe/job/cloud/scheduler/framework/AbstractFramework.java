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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.SchedulerDriver;

/**
 * 调度器框架.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractFramework {
    
    @Getter(AccessLevel.PACKAGE)
    private final CoordinatorRegistryCenter regCenter;
    
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private AbstractFramework delegate;
    
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private SchedulerDriver schedulerDriver;
    
    /**
     * 启动框架.
     *
     * @throws Exception 启动时抛出的异常
     */
    public abstract void start() throws Exception;
    
    /**
     * 关闭框架.
     */
    public abstract void stop();
}
