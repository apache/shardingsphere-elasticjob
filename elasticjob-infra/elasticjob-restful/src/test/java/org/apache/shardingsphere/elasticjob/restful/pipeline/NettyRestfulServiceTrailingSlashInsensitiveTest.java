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

package org.apache.shardingsphere.elasticjob.restful.pipeline;

import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.apache.shardingsphere.elasticjob.restful.controller.TrailingSlashTestController;
import org.junit.Test;

public final class NettyRestfulServiceTrailingSlashInsensitiveTest {
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 18082;
    
    @Test(expected = IllegalArgumentException.class)
    public void assertPathDuplicateWhenTrailingSlashInsensitive() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstances(new TrailingSlashTestController());
        RestfulService restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
    }
}
