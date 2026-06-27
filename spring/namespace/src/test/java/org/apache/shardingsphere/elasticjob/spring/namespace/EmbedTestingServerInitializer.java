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

package org.apache.shardingsphere.elasticjob.spring.namespace;

import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class EmbedTestingServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer();
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        EMBED_TESTING_SERVER.start();
        Map<String, Object> props = new HashMap<>();
        String connectionString = EMBED_TESTING_SERVER.getConnectionString();
        props.put("regCenter.serverLists", connectionString);
        props.put("regCenter1.serverLists", connectionString);
        props.put("regCenter2.serverLists", connectionString);
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("embedTestingServerProperties", props));
    }
}
