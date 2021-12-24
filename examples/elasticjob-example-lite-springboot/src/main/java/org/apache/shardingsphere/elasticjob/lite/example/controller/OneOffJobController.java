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

package org.apache.shardingsphere.elasticjob.lite.example.controller;

import javax.annotation.Resource;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OneOffJobController {
    
    private static final String RES_TEXT = "{\"msg\":\"OK\"}";
    
    @Resource(name = "manualScriptJobBean")
    private OneOffJobBootstrap manualScriptJob;

    @Autowired
    @Qualifier(value = "occurErrorNoticeDingtalkBean")
    private OneOffJobBootstrap occurErrorNoticeDingtalkJob;

    @Autowired
    @Qualifier(value = "occurErrorNoticeWechatBean")
    private OneOffJobBootstrap occurErrorNoticeWechatJob;

    @Autowired
    @Qualifier(value = "occurErrorNoticeEmailBean")
    private OneOffJobBootstrap occurErrorNoticeEmailJob;
    
    @GetMapping("/execute/manualScriptJob")
    public String executeManualScriptJob() {
        manualScriptJob.execute();
        return RES_TEXT;
    }
    
    @GetMapping("/execute/occurErrorNoticeDingtalkJob")
    public String executeOneOffJob() {
        occurErrorNoticeDingtalkJob.execute();
        return RES_TEXT;
    }
    
    @GetMapping("/execute/occurErrorNoticeWechatJob")
    public String executeOccurErrorNoticeWechatJob() {
        occurErrorNoticeWechatJob.execute();
        return RES_TEXT;
    }
    
    @GetMapping("/execute/occurErrorNoticeEmailJob")
    public String executeOccurErrorNoticeEmailJob() {
        occurErrorNoticeEmailJob.execute();
        return RES_TEXT;
    }
}
