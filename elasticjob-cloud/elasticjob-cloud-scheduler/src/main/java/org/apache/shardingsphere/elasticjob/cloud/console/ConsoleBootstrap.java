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

import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudAppController;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudJobController;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.CloudOperationController;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

/**
 * Console bootstrap for Cloud.
 */
public class ConsoleBootstrap {
    
    public ConsoleBootstrap(final CoordinatorRegistryCenter regCenter, final RestfulServerConfiguration config, final ProducerManager producerManager, final ReconcileService reconcileService) {
        ConsoleApplication.port = config.getPort();
        CloudJobController.init(regCenter, producerManager);
        CloudAppController.init(regCenter, producerManager);
        CloudOperationController.init(regCenter, reconcileService);
    }
    
    /**
     * Startup RESTful server.
     */
    public void start() {
        ConsoleApplication.start();
    }
    
    @SpringBootApplication
    public static class ConsoleApplication implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        
        @Setter
        private static int port;
        
        @Setter
        private static Class<?>[] extraSources;
        
        /**
         * Startup RESTful server.
         */
        public static void start() {
            SpringApplicationBuilder applicationBuilder = new SpringApplicationBuilder(ConsoleApplication.class);
            if (ArrayUtils.isNotEmpty(extraSources)) {
                applicationBuilder.sources(extraSources);
            }
            applicationBuilder.build().run();
        }
        
        @Override
        public void customize(final ConfigurableServletWebServerFactory factory) {
            if (port <= 0) {
                return;
            }
            factory.setPort(port);
        }
    }
}
