/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.lifecycle.restful.fixture;

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
    
    @POST
    @Path("/call")
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> call(final Map<String, String> map) {
        caller.call(map.get("string"));
        caller.call(Integer.valueOf(map.get("integer")));
        return Maps.transformEntries(map, new Maps.EntryTransformer<String, String, String>() {
            
            @Override
            public String transformEntry(final String key, final String value) {
                return value + "_processed";
            }
        });
    }
}
