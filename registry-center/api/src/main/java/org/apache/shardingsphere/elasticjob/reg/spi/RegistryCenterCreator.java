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

package org.apache.shardingsphere.elasticjob.reg.spi;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Registry center creator.
 * 
 * <p>Implementations of this interface are used to create different types of registry centers.</p>
 */
public interface RegistryCenterCreator {
    
    /**
     * Check if this creator supports the given connection string.
     *
     * @param connectString connection string
     * @return true if supported, false otherwise
     */
    boolean supports(String connectString);
    
    /**
     * Create a coordinator registry center.
     *
     * @param connectString connection string
     * @param namespace namespace
     * @param digest authentication information
     * @return coordinator registry center
     */
    CoordinatorRegistryCenter create(String connectString, String namespace, String digest);
}
