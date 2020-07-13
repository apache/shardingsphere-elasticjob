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

package org.apache.shardingsphere.elasticjob.reg.base;

import java.util.List;

/**
 * Coordinator registry center.
 */
public interface CoordinatorRegistryCenter extends RegistryCenter {
    
    /**
     * Get value from registry center directly.
     * 
     * @param key key
     * @return value
     */
    String getDirectly(String key);
    
    /**
     * Get children keys.
     * 
     * @param key key
     * @return children keys
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * Get children number.
     *
     * @param key key
     * @return children number
     */
    int getNumChildren(String key);
    
    /**
     * Persist ephemeral data.
     * 
     * @param key key
     * @param value value
     */
    void persistEphemeral(String key, String value);
    
    /**
     * Persist sequential data.
     *
     * @param key key
     * @param value value
     * @return value which include 10 digital
     */
    String persistSequential(String key, String value);
    
    /**
     * Persist ephemeral sequential data.
     * 
     * @param key key
     */
    void persistEphemeralSequential(String key);
    
    /**
     * Add data to cache.
     * 
     * @param cachePath cache path
     */
    void addCacheData(String cachePath);
    
    /**
     * Evict data from cache.
     *
     * @param cachePath cache path
     */
    void evictCacheData(String cachePath);
    
    /**
     * Get raw cache object of registry center.
     * 
     * @param cachePath cache path
     * @return raw cache object of registry center
     */
    Object getRawCache(String cachePath);
}
