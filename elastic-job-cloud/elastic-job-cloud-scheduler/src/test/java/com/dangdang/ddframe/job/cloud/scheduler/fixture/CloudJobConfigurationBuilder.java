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

package com.dangdang.ddframe.job.cloud.scheduler.fixture;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationBuilder {
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()), 
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, jobExecutionType);
    }
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType, final int shardingTotalCount) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", shardingTotalCount).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, jobExecutionType);
    }
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final boolean misfire) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(misfire).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createOtherCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createCloudSpringJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_spring_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT, "springSimpleJob", "applicationContext.xml");
    }
    
    public static CloudJobConfiguration createDataflowCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new DataflowJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(false).build(), SimpleJob.class.getCanonicalName(), true),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createScriptCloudJobConfiguration(final String jobName) {
        return createScriptCloudJobConfiguration(jobName, 3);
    }
    
    public static CloudJobConfiguration createScriptCloudJobConfiguration(final String jobName, final int shardingTotalCount) {
        return new CloudJobConfiguration("test_app",
                new ScriptJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", shardingTotalCount).failover(false).misfire(false).build(), "test.sh"),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
}
