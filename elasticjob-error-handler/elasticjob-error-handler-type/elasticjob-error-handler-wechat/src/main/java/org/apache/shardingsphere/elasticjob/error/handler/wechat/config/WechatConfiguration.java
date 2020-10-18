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

package org.apache.shardingsphere.elasticjob.error.handler.wechat.config;

import lombok.Getter;

import java.util.Properties;

/**
 * Wechat configuration.
 */
@Getter
public final class WechatConfiguration {
    
    private final String webhook;
    
    private final Integer connectTimeoutMillisecond;
    
    private final Integer readTimeoutMillisecond;
    
    public WechatConfiguration(final Properties props) {
        webhook = props.getProperty(WechatPropertiesConstants.WEBHOOK);
        connectTimeoutMillisecond = Integer.parseInt(props.getProperty(WechatPropertiesConstants.CONNECT_TIMEOUT_MILLISECOND, WechatPropertiesConstants.DEFAULT_CONNECT_TIMEOUT_MILLISECOND));
        readTimeoutMillisecond = Integer.parseInt(props.getProperty(WechatPropertiesConstants.READ_TIMEOUT_MILLISECOND, WechatPropertiesConstants.DEFAULT_READ_TIMEOUT_MILLISECOND));
    }
}
