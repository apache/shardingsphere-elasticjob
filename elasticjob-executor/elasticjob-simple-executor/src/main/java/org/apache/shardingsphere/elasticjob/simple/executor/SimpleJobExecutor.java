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

package org.apache.shardingsphere.elasticjob.simple.executor;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.executor.item.impl.ClassedJobItemExecutor;

/**
 * Simple job executor.
 */
public final class SimpleJobExecutor implements ClassedJobItemExecutor<SimpleJob> {
    
    @Override
    public void process(final SimpleJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        elasticJob.execute(shardingContext);
    }
    
    @Override
    public Class<SimpleJob> getElasticJobClass() {
        return SimpleJob.class;
    }
}
