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

package org.apache.shardingsphere.elasticjob.http.executor.fixture;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;

import java.util.Objects;

@Slf4j
public final class InternalController implements RestfulController {
    
    /**
     * Get name.
     *
     * @return "ejob"
     */
    @Mapping(method = Http.GET, path = "/getName")
    public String getName() {
        return "ejob";
    }
    
    /**
     * Get sharding context.
     *
     * @param shardingContext the shardingContext
     * @return shardingContext
     */
    @Mapping(method = Http.GET, path = "/getShardingContext")
    public String getShardingContext(@Param(name = "shardingContext", source = ParamSource.HEADER) final String shardingContext) {
        Objects.nonNull(shardingContext);
        return shardingContext;
    }
    
    /**
     * Update name.
     *
     * @param updateName the name
     * @return the updated name
     */
    @Mapping(method = Http.POST, path = "/{updateName}")
    public String postName(@Param(name = "updateName", source = ParamSource.PATH) final String updateName) {
        Objects.nonNull(updateName);
        return updateName;
    }
    
    /**
     * Post with 3 mills delay for request IO Exception.
     *
     * @return "ejob"
     */
    @Mapping(method = Http.POST, path = "/postWithTimeout")
    public String postWithTimeout() {
        try {
            Thread.sleep(3);
        } catch (InterruptedException ignore) {
        }
        return "ejob";
    }
}
