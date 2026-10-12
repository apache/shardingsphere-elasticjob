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
 * <p>{@code elasticjob.reg-center.type} selects which registry center is created, currently only {@code zookeeper} (default).
 * Other registry center types will be supported separately.</p>
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
     * Base sleep time milliseconds.
     */
    private int baseSleepTimeMilliseconds = 1000;
    
    /**
     * Max sleep time milliseconds.
     */
    private int maxSleepTimeMilliseconds = 3000;
    
    /**
     * Max retry times.
     */
    private int maxRetries = 3;
    
    /**
     * Session timeout milliseconds.
     */
    private int sessionTimeoutMilliseconds;
    
    /**
     * Connection timeout milliseconds.
     */
    private int connectionTimeoutMilliseconds;
    
    /**
     * ZooKeeper digest.
     */
    private String digest;
    
    /**
     * Whether the ensemble configuration changes are watched.
     */
    private boolean ensembleTracker = true;
}
