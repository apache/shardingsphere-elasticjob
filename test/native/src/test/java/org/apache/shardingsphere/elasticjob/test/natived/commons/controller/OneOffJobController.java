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

package org.apache.shardingsphere.elasticjob.test.natived.commons.controller;

import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OneOffJobController {
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("manualScriptJobBean")
    private ObjectProvider<OneOffJobBootstrap> manualScriptJobProvider;
    
    /**
     * Execute manual script job.
     *
     * @return a String
     */
    @GetMapping("/execute/manualScriptJob")
    public String executeManualScriptJob() {
        OneOffJobBootstrap manualScriptJob = manualScriptJobProvider.getIfAvailable();
        Objects.requireNonNull(manualScriptJob);
        manualScriptJob.execute();
        manualScriptJob.shutdown();
        return "{\"msg\":\"OK\"}";
    }
}
