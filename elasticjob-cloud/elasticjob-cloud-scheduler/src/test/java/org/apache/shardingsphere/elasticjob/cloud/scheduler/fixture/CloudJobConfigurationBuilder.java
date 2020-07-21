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
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationBuilder {
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createCloudJobConfiguration(final String jobName) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration("test_app", 1.0d, 128.0d, CloudJobExecutionType.TRANSIENT,
                JobConfiguration.newBuilder(jobName, 10).cron("0/30 * * * * ?").failover(true).misfire(true).build()));
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @param jobExecutionType job execution type
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration("test_app", 1.0d, 128.0d,
                jobExecutionType, JobConfiguration.newBuilder(jobName, 10).cron("0/30 * * * * ?").failover(true).misfire(true).build()));
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @param jobExecutionType execution type
     * @param shardingTotalCount sharding total count
     * @return cloud job configuration
     */
    public static CloudJobConfiguration createCloudJobConfiguration(final String jobName, final CloudJobExecutionType jobExecutionType, final int shardingTotalCount) {
        return new CloudJobConfiguration("test_app", 1.0d, 128.0d, jobExecutionType, 
                JobConfiguration.newBuilder(jobName, shardingTotalCount).cron("0/30 * * * * ?").failover(true).misfire(true).build());
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @param misfire misfire
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createCloudJobConfiguration(final String jobName, final boolean misfire) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration("test_app", 1.0d, 128.0d, CloudJobExecutionType.TRANSIENT,
                JobConfiguration.newBuilder(jobName, 10).cron("0/30 * * * * ?").failover(true).misfire(misfire).build()));
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @param appName app name
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createCloudJobConfiguration(final String jobName, final String appName) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration(appName, 1.0d, 128.0d,
                CloudJobExecutionType.TRANSIENT, JobConfiguration.newBuilder(jobName, 10).cron("0/30 * * * * ?").failover(true).misfire(true).build()));
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createOtherCloudJobConfiguration(final String jobName) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration(
                "test_app", 1.0d, 128.0d, CloudJobExecutionType.TRANSIENT, JobConfiguration.newBuilder(jobName, 3).cron("0/30 * * * * ?").failover(false).misfire(true).build()));
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @return cloud job configuration
     */
    public static CloudJobConfiguration createDataflowCloudJobConfiguration(final String jobName) {
        return new CloudJobConfiguration("test_app", 1.0d, 128.0d, CloudJobExecutionType.TRANSIENT, 
                JobConfiguration.newBuilder(jobName, 3).cron("0/30 * * * * ?").failover(false).misfire(false).setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).build());
    }
    
    /**
     * Create cloud job configuration.
     * 
     * @param jobName job name
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createScriptCloudJobConfiguration(final String jobName) {
        return createScriptCloudJobConfiguration(jobName, 3);
    }
    
    /**
     * Create script cloud job configuration.
     * 
     * @param jobName job name
     * @param shardingTotalCount sharding total count
     * @return cloud job configuration
     */
    public static CloudJobConfigurationPOJO createScriptCloudJobConfiguration(final String jobName, final int shardingTotalCount) {
        return CloudJobConfigurationPOJO.fromCloudJobConfiguration(new CloudJobConfiguration("test_app", 1.0d, 128.0d, CloudJobExecutionType.TRANSIENT,
                JobConfiguration.newBuilder(jobName, shardingTotalCount).cron("0/30 * * * * ?").failover(false).misfire(false).setProperty(ScriptJobProperties.SCRIPT_KEY, "test.sh").build()));
    }
}
