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

/**
 * Registry center.
 */
public interface RegistryCenter {
    
    /**
     * Initialize registry center.
     */
    void init();
    
    /**
     * Close registry center.
     */
    void close();
    
    /**
     * Get value.
     * 
     * @param key key
     * @return value
     */
    String get(String key);
    
    /**
     * Judge node is exist or not.
     * 
     * @param key key
     * @return node is exist or not
     */
    boolean isExisted(String key);
    
    /**
     * Persist data.
     * 
     * @param key key
     * @param value value
     */
    void persist(String key, String value);
    
    /**
     * Update data.
     * 
     * @param key key
     * @param value value
     */
    void update(String key, String value);
    
    /**
     * Remove data.
     * 
     * @param key key
     */
    void remove(String key);
    
    /**
     * Get current time from registry center.
     * 
     * @param key key
     * @return current time from registry center
     */
    long getRegistryCenterTime(String key);
    
    /**
     * Get raw client for registry center client.
     ** 
     * @return registry center raw client
     */
    Object getRawClient();
}
