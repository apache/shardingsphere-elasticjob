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

/**
 * Job error handler properties constants for send error message via email.
 */
public final class EmailPropertiesConstants {
    
    public static final String DEFAULT_IS_USE_SSL = Boolean.TRUE.toString();
    
    public static final String DEFAULT_SUBJECT = "ElasticJob error message";
    
    public static final String DEFAULT_IS_DEBUG = Boolean.FALSE.toString();
    
    private static final String PREFIX = "email.";
    
    public static final String HOST = PREFIX + "host";
    
    public static final String PORT = PREFIX + "port";
    
    public static final String USERNAME = PREFIX + "username";
    
    public static final String PASSWORD = PREFIX + "password";
    
    public static final String IS_USE_SSL = PREFIX + "useSsl";
    
    public static final String SUBJECT = PREFIX + "subject";
    
    public static final String FROM = PREFIX + "from";
    
    public static final String TO = PREFIX + "to";
    
    public static final String CC = PREFIX + "cc";
    
    public static final String BCC = PREFIX + "bcc";
    
    public static final String IS_DEBUG = PREFIX + "debug";
}
