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

package org.apache.shardingsphere.elasticjob.error.handler.wechat;

/**
 * Wechat constants.
 */
public final class WechatConstants {
    
    public static final String PREFIX = "wechat.";
    
    public static final String WECHAT_WEBHOOK = PREFIX + "webhook";
    
    public static final String WECHAT_CONNECT_TIMEOUT = PREFIX + "connectTimeout";
    
    public static final String WECHAT_READ_TIMEOUT = PREFIX + "readTimeout";
    
    public static final Integer DEFAULT_WECHAT_CONNECT_TIMEOUT = 3000;
    
    public static final Integer DEFAULT_WECHAT_READ_TIMEOUT = 5000;
}
