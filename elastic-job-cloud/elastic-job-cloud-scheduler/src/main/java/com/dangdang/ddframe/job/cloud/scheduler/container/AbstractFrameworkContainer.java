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

package com.dangdang.ddframe.job.cloud.scheduler.container;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.FrameworkMode;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.MesosConfiguration;
import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;

/**
 * 框架运行时容器.
 * 
 * @author gaohongtao
 */
@Slf4j
public abstract class AbstractFrameworkContainer implements ElectionCandidate {
    
    @Getter(AccessLevel.PROTECTED)
    private final CoordinatorRegistryCenter regCenter;
    
    AbstractFrameworkContainer(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    /**
     * 获取一个新的框架运行时容器.
     * 
     * @param coordinatorRegistryCenter 注册中心
     * @return 框架运行时容器
     */
    public static AbstractFrameworkContainer newFrameworkContainer(final CoordinatorRegistryCenter coordinatorRegistryCenter) {
        MesosConfiguration mesosConfig = BootstrapEnvironment.getInstance().getMesosConfiguration();
        Protos.FrameworkInfo.Builder frameworkInfoBuilder = Protos.FrameworkInfo.newBuilder().setUser(mesosConfig.getUser())
                .setName(MesosConfiguration.FRAMEWORK_NAME).setHostname(mesosConfig.getHostname());
        FrameworkMode mode = BootstrapEnvironment.getInstance().getFrameworkConfiguration().getMode();
        log.info("Elastic job: Elastic job Cloud running in {} mode", mode);
        final AbstractFrameworkContainer result;
        switch (mode) {
            case STANDALONE:
                result = new StandaloneFrameworkContainer(coordinatorRegistryCenter, frameworkInfoBuilder.build(), null);
                break;
            case HA: 
                result = new HAFrameworkContainer(coordinatorRegistryCenter, frameworkInfoBuilder);
                break;
            default: throw new IllegalArgumentException();
        }
        
        return result;
    }
    
    /**
     * 启动容器.
     * 
     * @throws Exception 启动容器时抛出的异常
     */
    public abstract void start() throws Exception;
    
    /**
     * 暂停容器.
     */
    public abstract void pause();
    
    /**
     * 从暂停中恢复回来.
     */
    public abstract void resume();
    
    /**
     * 关闭容器.
     * 关闭容器时请小心处理,避免抛出异常造成容器部分关闭
     */
    public abstract void shutdown();
    
    @Override
    public void startLeadership() throws Exception {
        start();
    }
    
    @Override
    public void stopLeadership() {
        shutdown();
    }
}
