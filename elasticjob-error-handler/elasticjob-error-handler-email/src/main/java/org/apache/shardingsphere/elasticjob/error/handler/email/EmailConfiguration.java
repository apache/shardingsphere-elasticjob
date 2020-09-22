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

package org.apache.shardingsphere.elasticjob.error.handler.email;

import lombok.Getter;
import lombok.Setter;

/**
 * Email configuration POJO.
 */
@Getter
@Setter
public final class EmailConfiguration {
    
    private String host;
    
    private Integer port;
    
    private String username;
    
    private String password;
    
    private String protocol = "smtp";
    
    private String subject = "ElasticJob error message";
    
    private String from;
    
    private String to;
    
    private String cc;
    
    private String bcc;
}
