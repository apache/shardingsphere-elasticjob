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

package org.apache.shardingsphere.elasticjob.lite.spring.fixture.job.ref;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.spring.fixture.service.FooService;

import java.util.Collections;
import java.util.List;

public class RefFooDataflowElasticJob implements DataflowJob<String> {
    
    @Getter
    private static volatile boolean completed;
    
    @Getter
    @Setter
    private FooService fooService;
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        if (completed) {
            return Collections.emptyList();
        }
        fooService.foo();
        return Collections.singletonList("data");
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        completed = true;
    }
    
    /**
     * Set completed to false.
     */
    public static void reset() {
        completed = false;
    }
}
