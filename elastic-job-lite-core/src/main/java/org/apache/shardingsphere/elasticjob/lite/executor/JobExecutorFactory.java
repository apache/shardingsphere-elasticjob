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

package org.apache.shardingsphere.elasticjob.lite.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.api.simple.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.executor.type.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.SimpleJobExecutor;

/**
 * Job executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobExecutorFactory {
    
    /**
     * Get job executor.
     *
     * @param elasticJob elasticJob object
     * @param jobFacade job facade
     * @return job executor
     */
    @SuppressWarnings("unchecked")
    public static ElasticJobExecutor getJobExecutor(final ElasticJob elasticJob, final JobFacade jobFacade) {
        if (null == elasticJob) {
            return new ScriptJobExecutor(jobFacade);
        }
        if (elasticJob instanceof SimpleJob) {
            return new SimpleJobExecutor((SimpleJob) elasticJob, jobFacade);
        }
        if (elasticJob instanceof DataflowJob) {
            return new DataflowJobExecutor((DataflowJob) elasticJob, jobFacade);
        }
        throw new JobConfigurationException("Cannot support job type '%s'", elasticJob.getClass().getCanonicalName());
    }
}
