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

package org.apache.shardingsphere.elasticjob.test.e2e.raw.fixture.listener;

import org.apache.shardingsphere.elasticjob.kernel.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.AbstractDistributeOnceElasticJobListener;

public class DistributeOnceE2EFixtureJobListener extends AbstractDistributeOnceElasticJobListener {
    
    public DistributeOnceE2EFixtureJobListener() {
        super(100L, 100L);
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public String getType() {
        return "INTEGRATE-DISTRIBUTE";
    }
}
