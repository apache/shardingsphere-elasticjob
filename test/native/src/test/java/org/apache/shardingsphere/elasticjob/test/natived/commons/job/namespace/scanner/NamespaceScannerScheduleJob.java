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

package org.apache.shardingsphere.elasticjob.test.natived.commons.job.namespace.scanner;

import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Schedule job registered by the Spring Namespace {@code elasticjob:job-scanner} in native tests.
 *
 * <p>The job name differs from the bean name, because the scanner skips a bootstrap whose name is already taken.</p>
 */
@ElasticJobConfiguration(jobName = "scannerScheduleJob", cron = "0/1 * * * * ?", shardingTotalCount = 1)
public class NamespaceScannerScheduleJob implements SimpleJob {
    
    private final AtomicInteger executionCount = new AtomicInteger();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        executionCount.incrementAndGet();
    }
    
    /**
     * Get the number of executions.
     *
     * @return execution count
     */
    public int getExecutionCount() {
        return executionCount.get();
    }
}
