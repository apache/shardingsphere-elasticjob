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

package com.dangdang.ddframe.job.cloud.state.fixture;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.JobExecutionType;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import com.dangdang.ddframe.job.event.log.JobLogEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobRdbEventConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationBuilder {
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()), 
                1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", "bin/start.sh", JobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final JobExecutionType jobExecutionType) {
        return new CloudJobConfiguration(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", "bin/start.sh", jobExecutionType);
    }
    
    public static CloudJobConfiguration createOtherCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", "bin/start.sh", JobExecutionType.TRANSIENT);
    }
    
    public static CloudJobConfiguration createCloudJobConfigurationWithEventConfiguration(final String jobName) {
        JobRdbEventConfiguration rdbEventConfig = new JobRdbEventConfiguration("org.h2.Driver", "jdbc:h2:mem:job_event_storage", "sa", "", LogLevel.INFO);
        JobLogEventConfiguration logEventConfig = new JobLogEventConfiguration();
        return new CloudJobConfiguration(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(false)
                .jobEventConfiguration(rdbEventConfig, logEventConfig).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", "bin/start.sh", JobExecutionType.TRANSIENT);
    }
}
