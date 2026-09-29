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

package org.apache.shardingsphere.elasticjob.reg.nacos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Nacos configuration.
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class NacosConfiguration {
    
    /**
     * Server list of Nacos.
     *
     * <p>
     * Include IP addresses and ports,
     * Multiple IP address split by comma.
     * For example: 127.0.0.1:8848,127.0.0.2:8848
     * The {@code nacos://} prefix is optional and will be stripped automatically.
     * </p>
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    private final String serverLists;
    
    /**
     * Namespace, mapped to Nacos config group.
     */
    private final String namespace;
    
    /**
     * Nacos tenant (namespace id), empty means public.
     */
    private String nacosNamespace = "";
    
    /**
     * Username for authentication.
     */
    private String username;
    
    /**
     * Password for authentication.
     */
    private String password;
    
    /**
     * Timeout milliseconds of config operations.
     */
    private long timeoutMs = 3000L;
}
