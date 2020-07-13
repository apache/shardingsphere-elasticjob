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

package org.apache.shardingsphere.elasticjob.cloud.executor.local.fixture;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class TestSimpleJob implements SimpleJob {
    
    @Getter
    @Setter
    private static ShardingContext shardingContext;
    
    @Getter
    private static Set<String> shardingParameters = new ConcurrentSkipListSet<>();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        TestSimpleJob.shardingContext = shardingContext;
        if (!Strings.isNullOrEmpty(shardingContext.getShardingParameter())) {
            shardingParameters.add(shardingContext.getShardingParameter());    
        }
    }
}
