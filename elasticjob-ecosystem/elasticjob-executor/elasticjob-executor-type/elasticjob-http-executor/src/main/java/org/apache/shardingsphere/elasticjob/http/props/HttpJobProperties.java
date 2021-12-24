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

package org.apache.shardingsphere.elasticjob.http.props;

/**
 * HTTP job properties.
 */
public final class HttpJobProperties {
    
    /**
     * HTTP request URI.
     */
    public static final String URI_KEY = "http.uri";
    
    /**
     * Http request method.
     */
    public static final String METHOD_KEY = "http.method";
    
    /**
     * HTTP request data.
     */
    public static final String DATA_KEY = "http.data";
    
    /**
     * HTTP connect timeout in milliseconds.
     */
    public static final String CONNECT_TIMEOUT_KEY = "http.connect.timeout.milliseconds";
    
    /**
     * HTTP read timeout in milliseconds.
     */
    public static final String READ_TIMEOUT_KEY = "http.read.timeout.milliseconds";
    
    /**
     * HTTP content type.
     */
    public static final String CONTENT_TYPE_KEY = "http.content.type";
    
    /**
     * HTTP sharding context.
     */
    public static final String SHARDING_CONTEXT_KEY = "shardingContext";
}
