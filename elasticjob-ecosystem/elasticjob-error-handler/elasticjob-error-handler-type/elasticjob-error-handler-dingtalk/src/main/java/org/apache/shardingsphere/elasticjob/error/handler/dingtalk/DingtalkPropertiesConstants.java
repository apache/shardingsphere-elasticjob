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

/**
 * Job error handler properties constants for send error message via dingtalk.
 */
public final class DingtalkPropertiesConstants {
    
    public static final String DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = "3000";
    
    public static final String DEFAULT_READ_TIMEOUT_MILLISECONDS = "5000";
    
    private static final String PREFIX = "dingtalk.";
    
    public static final String WEBHOOK = PREFIX + "webhook";
    
    public static final String KEYWORD = PREFIX + "keyword";
    
    public static final String SECRET = PREFIX + "secret";
    
    public static final String CONNECT_TIMEOUT_MILLISECONDS = PREFIX + "connectTimeoutMilliseconds";
    
    public static final String READ_TIMEOUT_MILLISECONDS = PREFIX + "readTimeoutMilliseconds";
}
