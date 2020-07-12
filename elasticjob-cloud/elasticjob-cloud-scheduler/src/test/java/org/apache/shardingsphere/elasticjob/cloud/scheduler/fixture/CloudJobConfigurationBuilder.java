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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationBuilder {

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @param jobExecutionType execution type
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, jobExecutionType);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @param jobExecutionType execution type
     * @param shardingTotalCount sharding total count
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType, final int shardingTotalCount) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", shardingTotalCount).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, jobExecutionType);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @param misfire misfire
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final boolean misfire) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(misfire).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @param appName app name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final String appName) {
        return new CloudJobConfiguration(appName,
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createOtherCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createCloudSpringJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_spring_app",
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 10).failover(true).misfire(true).build(), TestSimpleJob.class.getCanonicalName()),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT, "springSimpleJob", "applicationContext.xml");
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createDataflowCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app",
                new DataflowJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", 3).failover(false).misfire(false).build(), SimpleJob.class.getCanonicalName(), true),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }

    /**
     * Create cloud job configuration.
     * @param jobName job name
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createScriptCloudJobConfiguration(final String jobName) {
        return createScriptCloudJobConfiguration(jobName, 3);
    }

    /**
     * Create script cloud job configuration.
     * @param jobName job name
     * @param shardingTotalCount sharding total count
     * @return CloudJobConfiguration
     */
    public static CloudJobConfiguration createScriptCloudJobConfiguration(final String jobName, final int shardingTotalCount) {
        return new CloudJobConfiguration("test_app",
                new ScriptJobConfiguration(JobCoreConfiguration.newBuilder(jobName, "0/30 * * * * ?", shardingTotalCount).failover(false).misfire(false).build(), "test.sh"),
                1.0d, 128.0d, CloudJobExecutionType.TRANSIENT);
    }
}
