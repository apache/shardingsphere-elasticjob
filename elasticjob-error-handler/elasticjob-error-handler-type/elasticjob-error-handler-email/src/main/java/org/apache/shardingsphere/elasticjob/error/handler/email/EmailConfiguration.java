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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
     * Create email configuration builder.
     *
     * @param host host
     * @param port port
     * @param username username
     * @param password password
     * @param from from
     * @param to to
     * @return email configuration builder
     */
    public static Builder newBuilder(final String host, final int port, final String username, final String password, final String from, final String to) {
        return new Builder(host, port, username, password, from, to);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        
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
         * Set whether use SSL.
         *
         * @param useSsl use SSL or not
         * @return email configuration builder
         */
        public Builder useSsl(final boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }
        
        /**
         * Set subject.
         *
         * @param subject subject
         * @return email configuration builder
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
         * @return email configuration builder
         */
        public Builder cc(final String cc) {
            this.cc = cc;
            return this;
        }
        
        /**
         * Set bcc.
         *
         * @param bcc bcc
         * @return email configuration builder
         */
        public Builder bcc(final String bcc) {
            this.bcc = bcc;
            return this;
        }
        
        /**
         * Set whether use debug mode.
         *
         * @param debug use debug mode or not
         * @return email configuration builder
         */
        public Builder debug(final boolean debug) {
            this.debug = debug;
            return this;
        }
        
        /**
         * Build email configuration.
         *
         * @return email configuration
         */
        public EmailConfiguration build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "host can not be empty.");
            Preconditions.checkArgument(port > 0 && port < 65535, "port should larger than 0 and small than 65535.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "username can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "password can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(from), "from can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(to), "to can not be empty.");
            return new EmailConfiguration(host, port, username, password, useSsl, subject, from, to, cc, bcc, debug);
        }
    }
}
