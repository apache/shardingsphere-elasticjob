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

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationListener;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationNode;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudJobRestfulApi;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.mesos.SchedulerDriver;

import java.util.concurrent.Executors;

/**
 * 用户服务框架.
 * 
 * @author gaohongtao
 */
@Slf4j
class UserServiceFramework extends AbstractFramework {
    
    @Setter(AccessLevel.PACKAGE)
    private SchedulerDriver schedulerDriver;
    
    private final RestfulServer restfulServer;
    
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    UserServiceFramework(final CoordinatorRegistryCenter regCenter) {
        super(regCenter);
        restfulServer = new RestfulServer(BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getPort());
        CloudJobRestfulApi.init(getRegCenter());
    }
    
    @Override
    public void start() throws Exception {
        CloudJobRestfulApi.start(schedulerDriver);
        cloudJobConfigurationListener =  new CloudJobConfigurationListener(schedulerDriver, getRegCenter());
        getCache().getListenable().addListener(cloudJobConfigurationListener, Executors.newSingleThreadExecutor());
        Frameworks.invoke("Start REST server", new Frameworks.Invokable() {
            @Override
            public void invoke() throws Exception {
                restfulServer.start(CloudJobRestfulApi.class.getPackage().getName());
            }
        });
    }
    
    private TreeCache getCache() {
        TreeCache result = (TreeCache) getRegCenter().getRawCache(ConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        getRegCenter().addCacheData(ConfigurationNode.ROOT);
        return (TreeCache) getRegCenter().getRawCache(ConfigurationNode.ROOT);
    }
    
    @Override
    public synchronized void stop() {
        getCache().getListenable().removeListener(cloudJobConfigurationListener);
        Frameworks.safetyInvoke("Shutdown REST server", new Frameworks.Invokable() {
    
            @Override
            public void invoke() throws Exception {
                restfulServer.stop();
            }
        });
        CloudJobRestfulApi.stop();
    }
}
