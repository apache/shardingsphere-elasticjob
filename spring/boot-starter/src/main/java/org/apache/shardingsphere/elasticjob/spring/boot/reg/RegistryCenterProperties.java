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

package org.apache.shardingsphere.elasticjob.spring.boot.reg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties of the ElasticJob registry center.
 *
 * <p>The properties are shared by all the registry center types.
 * {@code elasticjob.reg-center.type} selects which registry center is created:
 * {@code zookeeper} (default), {@code etcd}, {@code nacos} or {@code memory}.
 * Properties that are only valid for a specific registry center are ignored by the other types,
 * and a value of {@code 0} for a timeout means that the built-in default of the registry center is used.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "elasticjob.reg-center")
public class RegistryCenterProperties {
    
    /**
     * Type of the registry center.
     */
    private String type = "zookeeper";
    
    /**
     * Server list of the registry center.
     *
     * <p>Include IP addresses and ports,
     * multiple IP addresses split by comma.
     * For example: host1:2181,host2:2181</p>
     */
    private String serverLists;
    
    /**
     * Namespace.
     */
    private String namespace;
    
    /**
     * Base sleep time milliseconds, only used by ZooKeeper.
     */
    private int baseSleepTimeMilliseconds = 1000;
    
    /**
     * Max sleep time milliseconds, only used by ZooKeeper.
     */
    private int maxSleepTimeMilliseconds = 3000;
    
    /**
     * Max retry times, only used by ZooKeeper.
     */
    private int maxRetries = 3;
    
    /**
     * Session timeout milliseconds, only used by ZooKeeper.
     */
    private int sessionTimeoutMilliseconds;
    
    /**
     * Connection timeout milliseconds, used by ZooKeeper and etcd.
     */
    private int connectionTimeoutMilliseconds;
    
    /**
     * ZooKeeper digest, only used by ZooKeeper.
     */
    private String digest;
    
    /**
     * Whether the ensemble configuration changes are watched, only used by ZooKeeper.
     */
    private boolean ensembleTracker = true;
    
    /**
     * Username for authentication, used by etcd and Nacos.
     */
    private String username;
    
    /**
     * Password for authentication, used by etcd and Nacos.
     */
    private String password;
    
    /**
     * Whether to use HTTPs, only used by etcd.
     */
    private boolean ssl;
    
    /**
     * Authority header for HTTP/2, only used by etcd.
     */
    private String authority;
    
    /**
     * Nacos tenant (namespace id), only used by Nacos.
     *
     * <p>Distinct from the ElasticJob {@code namespace}, which is mapped to the Nacos config group.</p>
     */
    private String tenant;
    
    /**
     * Timeout milliseconds of config operations, only used by Nacos.
     */
    private long timeoutMs;
}
