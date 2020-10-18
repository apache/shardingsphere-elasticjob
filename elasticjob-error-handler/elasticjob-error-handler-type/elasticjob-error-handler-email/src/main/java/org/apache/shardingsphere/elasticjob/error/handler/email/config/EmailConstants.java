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

package org.apache.shardingsphere.elasticjob.error.handler.email.config;

/**
 * Job error handler properties constants for send error message via email.
 */
public final class EmailConstants {
    
    public static final String PREFIX = "email.";
    
    public static final String EMAIL_HOST = PREFIX + "host";
    
    public static final String EMAIL_PORT = PREFIX + "port";
    
    public static final String EMAIL_USERNAME = PREFIX + "username";
    
    public static final String EMAIL_PASSWORD = PREFIX + "password";
    
    public static final String EMAIL_USE_SSL = PREFIX + "useSsl";
    
    public static final String EMAIL_SUBJECT = PREFIX + "subject";
    
    public static final String EMAIL_FROM = PREFIX + "from";
    
    public static final String EMAIL_TO = PREFIX + "to";
    
    public static final String EMAIL_CC = PREFIX + "cc";
    
    public static final String EMAIL_BCC = PREFIX + "bcc";
    
    public static final String EMAIL_DEBUG = PREFIX + "debug";
    
    public static final String DEFAULT_EMAIL_SUBJECT = "ElasticJob error message";
}
