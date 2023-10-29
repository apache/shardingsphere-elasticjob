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

package org.apache.shardingsphere.elasticjob.test.e2e.fixture.executor;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.spi.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.spi.param.ShardingContext;
import org.apache.shardingsphere.elasticjob.spi.type.ClassedJobItemExecutor;
import org.apache.shardingsphere.elasticjob.test.e2e.fixture.job.E2EFixtureJob;

public final class E2EFixtureJobExecutor implements ClassedJobItemExecutor<E2EFixtureJob> {
    
    @Override
    public void process(final E2EFixtureJob elasticJob, final JobConfiguration jobConfig, final JobRuntimeService jobRuntimeService, final ShardingContext shardingContext) {
        elasticJob.foo(shardingContext);
    }
    
    @Override
    public Class<E2EFixtureJob> getElasticJobClass() {
        return E2EFixtureJob.class;
    }
}
