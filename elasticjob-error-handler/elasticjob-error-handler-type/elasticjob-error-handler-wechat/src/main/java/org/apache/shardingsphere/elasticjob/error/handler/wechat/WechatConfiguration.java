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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Wechat configuration.
 */
@Getter
@RequiredArgsConstructor
public final class WechatConfiguration {
    
    private final String webhook;
    
    private final Integer connectTimeout;
    
    private final Integer readTimeout;
    
    /**
     * Get wechat configuration.
     *
     * @param props properties
     * @return wechat configuration
     */
    public static WechatConfiguration getByProps(final Properties props) {
        return new WechatConfiguration(props.getProperty(WechatConstants.WECHAT_WEBHOOK),
                Integer.valueOf(props.getOrDefault(WechatConstants.WECHAT_CONNECT_TIMEOUT, WechatConstants.DEFAULT_WECHAT_CONNECT_TIMEOUT).toString()),
                Integer.valueOf(props.getOrDefault(WechatConstants.WECHAT_READ_TIMEOUT, WechatConstants.DEFAULT_WECHAT_READ_TIMEOUT).toString()));
    }
}
