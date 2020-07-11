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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import org.apache.shardingsphere.elasticjob.cloud.restful.RestfulServer;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.security.WwwAuthFilter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Restful server.
 */
public final class RestfulService {
    
    private static final String CONSOLE_PATH = "console";
    
    private final RestfulServer restfulServer;
    
    public RestfulService(final CoordinatorRegistryCenter regCenter, final RestfulServerConfiguration config, final ProducerManager producerManager, final ReconcileService reconcileService) {
        restfulServer = new RestfulServer(config.getPort());
        CloudJobRestfulApi.init(regCenter, producerManager);
        CloudAppRestfulApi.init(regCenter, producerManager);
        CloudOperationRestfulApi.init(regCenter, reconcileService);
    }
    
    /**
     * Start restful server.
     */
    public void start() {
        try {
            restfulServer.addFilter(WwwAuthFilter.class, "*/")
                         .addFilter(WwwAuthFilter.class, "*.html")
                         .start(RestfulService.class.getPackage().getName(), CONSOLE_PATH);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            throw new RuntimeException(ex.getCause());
        }
    }
    
    /**
     * Stop restful server.
     */
    public void stop() {
        restfulServer.stop();
    }
}
