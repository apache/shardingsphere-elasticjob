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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import com.dangdang.ddframe.job.cloud.scheduler.env.RestfulServerConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;

/**
 * 云作业Restful服务.
 *
 * @author caohao
 */
public class RestfulService {
    
    private static final String CONSOLE_PATH = "console";
    
    private final RestfulServer restfulServer;
    
    public RestfulService(final CoordinatorRegistryCenter regCenter, final RestfulServerConfiguration config, final ProducerManager producerManager) {
        restfulServer = new RestfulServer(config.getPort());
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudJobConfigurationGsonFactory.CloudJobConfigurationGsonTypeAdapter());
        GsonFactory.registerTypeAdapter(CloudAppConfiguration.class, new CloudAppConfigurationGsonFactory.CloudAppConfigurationGsonTypeAdapter());
        CloudJobRestfulApi.init(regCenter, producerManager);
        CloudAppRestfulApi.init(regCenter);
        CloudOperationRestfulApi.init(regCenter, producerManager);
    }
    
    /**
     * 启动Restful服务.
     */
    public void start() {
        try {
            restfulServer.start(RestfulService.class.getPackage().getName(), Optional.of(CONSOLE_PATH));
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            throw new RuntimeException(ex.getCause());
        }
    }
    
    /**
     * 停止Restful服务.
     */
    public void stop() {
        restfulServer.stop();
    }
}
