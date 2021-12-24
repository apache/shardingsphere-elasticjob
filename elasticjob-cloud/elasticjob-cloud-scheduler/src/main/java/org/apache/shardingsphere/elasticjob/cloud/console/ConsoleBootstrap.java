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

package org.apache.shardingsphere.elasticjob.cloud.console;

import org.apache.shardingsphere.elasticjob.cloud.console.config.advice.ConsoleExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudAppController;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudJobController;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudOperationController;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationFilter;
import org.apache.shardingsphere.elasticjob.cloud.console.security.AuthenticationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;

/**
 * Console bootstrap for Cloud.
 */
public class ConsoleBootstrap {
    
    private final RestfulService restfulService;
    
    public ConsoleBootstrap(final CoordinatorRegistryCenter regCenter, final RestfulServerConfiguration config, final ProducerManager producerManager, final ReconcileService reconcileService) {
        CloudJobController.init(regCenter, producerManager);
        CloudAppController.init(regCenter, producerManager);
        CloudOperationController.init(regCenter, reconcileService);
        NettyRestfulServiceConfiguration restfulServiceConfiguration = new NettyRestfulServiceConfiguration(config.getPort());
        restfulServiceConfiguration.addControllerInstances(new CloudJobController(), new CloudAppController(), new CloudOperationController());
        restfulServiceConfiguration.addExceptionHandler(Exception.class, new ConsoleExceptionHandler());
        restfulServiceConfiguration.addFilterInstances(new AuthenticationFilter(new AuthenticationService()));
        restfulService = new NettyRestfulService(restfulServiceConfiguration);
    }
    
    /**
     * Startup RESTful server.
     */
    public void start() {
        restfulService.startup();
    }
    
    /**
     * Stop RESTful server.
     */
    public void stop() {
        restfulService.shutdown();
    }
}
