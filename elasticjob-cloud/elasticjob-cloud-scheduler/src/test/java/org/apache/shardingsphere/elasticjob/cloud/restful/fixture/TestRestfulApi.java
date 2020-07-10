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

package org.apache.shardingsphere.elasticjob.cloud.restful.fixture;

import com.google.common.collect.Maps;
import lombok.Setter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/test")
public final class TestRestfulApi {
    
    @Setter
    private static Caller caller;

    /**
     * Test restful api is working.
     * @param map request parameters
     * @return map result
     */
    @POST
    @Path("/call")
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> call(final Map<String, String> map) {
        caller.call(map.get("string"));
        caller.call(Integer.valueOf(map.get("integer")));
        return Maps.transformEntries(map, (key, value) -> value + "_processed");
    }
}
