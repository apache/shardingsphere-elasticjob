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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
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
    
    private final int connectTimeoutMillisecond;
    
    private final int readTimeoutMillisecond;
    
    @Override
    public String getType() {
        return DingtalkType.TYPE;
    }
    
    /**
     * Create DingTalk configuration builder.
     *
     * @param webhook webhook
     * @param keyword keyword
     * @param secret  secret
     * @return DingTalk configuration builder
     */
    public static Builder newBuilder(final String webhook, final String keyword,
                                     final String secret) {
        return new Builder(webhook, keyword, secret);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        
        private final String webhook;
        
        private final String keyword;
        
        private final String secret;
        
        private int connectTimeoutMillisecond = 3000;
        
        private int readTimeoutMillisecond = 5000;
        
        /**
         * Set connect timeout.
         *
         * @param connectTimeoutMillisecond connect timeout
         * @return DingTalk configuration builder
         */
        public Builder connectTimeoutMillisecond(final int connectTimeoutMillisecond) {
            this.connectTimeoutMillisecond = connectTimeoutMillisecond;
            return this;
        }
        
        /**
         * Set read timeout.
         *
         * @param readTimeoutMillisecond read timeout
         * @return DingTalk configuration builder
         */
        public Builder readTimeoutMillisecond(final int readTimeoutMillisecond) {
            this.readTimeoutMillisecond = readTimeoutMillisecond;
            return this;
        }
        
        /**
         * Build DingTalk configuration.
         *
         * @return DingTalk configuration
         */
        public final DingtalkConfiguration build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(webhook), "webhook can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(keyword), "keyword can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(secret), "secret can not be empty.");
            return new DingtalkConfiguration(webhook, keyword, secret, connectTimeoutMillisecond, readTimeoutMillisecond);
        }
    }
}
