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

package org.apache.shardingsphere.elasticjob.kernel.api.listener.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.spi.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;

@RequiredArgsConstructor
public final class TestElasticJobListener implements ElasticJobListener {
    
    private final ElasticJobListenerCaller caller;
    
    private final String name;
    
    private final int order;
    
    private final StringBuilder orderResult;
    
    public TestElasticJobListener() {
        this(null, null, 0, new StringBuilder());
    }
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        caller.before();
        orderResult.append(name);
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        caller.after();
        orderResult.append(name);
    }
    
    @Override
    public String getType() {
        return "TEST";
    }
    
    @Override
    public int order() {
        return order;
    }
}
