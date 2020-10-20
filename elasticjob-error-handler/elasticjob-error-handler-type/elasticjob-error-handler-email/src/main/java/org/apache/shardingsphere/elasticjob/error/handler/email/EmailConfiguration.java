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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
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
    
    private final boolean useSsl;
    
    private final String subject;
    
    private final String from;
    
    private final String to;
    
    private final String cc;
    
    private final String bcc;
    
    private final boolean debug;
    
    @Override
    public String getType() {
        return EmailType.TYPE;
    }
    
    /**
     * Create Email configuration builder.
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @param from     from
     * @param to       to
     * @return Email configuration builder
     */
    public static Builder newBuilder(final String host, final int port, final String username,
                                     final String password, final String from, final String to) {
        return new Builder(host, port, username, password, from, to);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        
        private final String host;
        
        private final int port;
        
        private final String username;
        
        private final String password;
        
        private boolean useSsl = true;
        
        private String subject = "ElasticJob error message";
        
        private final String from;
        
        private final String to;
        
        private String cc;
        
        private String bcc;
        
        private boolean debug;
        
        /**
         * Set useSsl.
         *
         * @param useSsl useSsl
         * @return Email configuration builder
         */
        public Builder useSsl(final boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }
        
        /**
         * Set subject.
         *
         * @param subject subject
         * @return Email configuration builder
         */
        public Builder subject(final String subject) {
            if (!Strings.isNullOrEmpty(subject)) {
                this.subject = subject;
            }
            return this;
        }
        
        /**
         * Set cc.
         *
         * @param cc cc
         * @return Email configuration builder
         */
        public Builder cc(final String cc) {
            this.cc = cc;
            return this;
        }
        
        /**
         * Set bcc.
         *
         * @param bcc bcc
         * @return Email configuration builder
         */
        public Builder bcc(final String bcc) {
            this.bcc = bcc;
            return this;
        }
        
        /**
         * Set debug.
         *
         * @param debug debug
         * @return Email configuration builder
         */
        public Builder debug(final boolean debug) {
            this.debug = debug;
            return this;
        }
        
        /**
         * Build Email configuration.
         *
         * @return Email configuration
         */
        public final EmailConfiguration build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "host can not be empty.");
            Preconditions.checkArgument(0 < port && 65535 < port, "port should larger than 0 and small than 65535.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "username can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "password can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(from), "from can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(to), "to can not be empty.");
            return new EmailConfiguration(host, port, username, password, useSsl, subject,
                    from, to, cc, bcc, debug);
        }
    }
}
