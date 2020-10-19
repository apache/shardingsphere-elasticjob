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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.error.handler.ErrorHandlerConfiguration;

/**
 * Job error handler configuration for send error message via email.
 */
@RequiredArgsConstructor
@Getter
public final class EmailConfiguration implements ErrorHandlerConfiguration {
    
    private final String host;
    
    private final int port;
    
    private final String username;
    
    private final String password;
    
    // TODO default value is true
    private final boolean useSsl;
    
    // TODO default value is ElasticJob error message
    private final String subject;
    
    private final String from;
    
    private final String to;
    
    private final String cc;
    
    private final String bcc;
    
    // TODO default value is false
    private final boolean debug;
    
    @Override
    public String getType() {
        return EmailType.TYPE;
    }
}
