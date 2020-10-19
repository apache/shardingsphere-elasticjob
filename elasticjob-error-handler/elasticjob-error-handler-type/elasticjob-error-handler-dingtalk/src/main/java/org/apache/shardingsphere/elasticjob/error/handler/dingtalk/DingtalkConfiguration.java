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

package org.apache.shardingsphere.elasticjob.error.handler.dingtalk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.error.handler.ErrorHandlerConfiguration;

/**
 * Job error handler configuration for send error message via dingtalk.
 */
@RequiredArgsConstructor
@Getter
public final class DingtalkConfiguration implements ErrorHandlerConfiguration {
    
    private final String webhook;
    
    private final String keyword;
    
    private final String secret;
    
    // TODO default value is 3000
    private final int connectTimeoutMillisecond;

    // TODO default value is 5000
    private final int readTimeoutMillisecond;
    
    @Override
    public String getType() {
        return DingtalkType.TYPE;
    }
}
