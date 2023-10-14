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

package org.apache.shardingsphere.elasticjob.lite.internal.annotation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfigurationFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;

/**
 * Job Builder from @ElasticJobConfiguration.
 */
public final class JobAnnotationBuilder {
    
    /**
     * generate JobConfiguration from @ElasticJobConfiguration.
     * @param type The job of @ElasticJobConfiguration annotation class
     * @return JobConfiguration
     */
    public static JobConfiguration generateJobConfiguration(final Class<?> type) {
        ElasticJobConfiguration annotation = type.getAnnotation(ElasticJobConfiguration.class);
        Preconditions.checkArgument(null != annotation, "@ElasticJobConfiguration not found by class '%s'.", type);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(annotation.jobName()), "@ElasticJobConfiguration jobName could not be empty by class '%s'.", type);
        JobConfiguration.Builder jobConfigurationBuilder = JobConfiguration.newBuilder(annotation.jobName(), annotation.shardingTotalCount())
                .shardingItemParameters(annotation.shardingItemParameters())
                .cron(Strings.isNullOrEmpty(annotation.cron()) ? null : annotation.cron())
                .timeZone(Strings.isNullOrEmpty(annotation.timeZone()) ? null : annotation.timeZone())
                .jobParameter(annotation.jobParameter())
                .monitorExecution(annotation.monitorExecution())
                .failover(annotation.failover())
                .misfire(annotation.misfire())
                .maxTimeDiffSeconds(annotation.maxTimeDiffSeconds())
                .reconcileIntervalMinutes(annotation.reconcileIntervalMinutes())
                .jobShardingStrategyType(Strings.isNullOrEmpty(annotation.jobShardingStrategyType()) ? null : annotation.jobShardingStrategyType())
                .jobExecutorServiceHandlerType(Strings.isNullOrEmpty(annotation.jobExecutorServiceHandlerType()) ? null : annotation.jobExecutorServiceHandlerType())
                .jobErrorHandlerType(Strings.isNullOrEmpty(annotation.jobErrorHandlerType()) ? null : annotation.jobErrorHandlerType())
                .jobListenerTypes(annotation.jobListenerTypes())
                .description(annotation.description())
                .disabled(annotation.disabled())
                .overwrite(annotation.overwrite());
        for (Class<? extends JobExtraConfigurationFactory> clazz : annotation.extraConfigurations()) {
            try {
                Optional<JobExtraConfiguration> jobExtraConfiguration = clazz.newInstance().getJobExtraConfiguration();
                jobExtraConfiguration.ifPresent(jobConfigurationBuilder::addExtraConfigurations);
            } catch (IllegalAccessException | InstantiationException exception) {
                throw (JobConfigurationException) new JobConfigurationException("new JobExtraConfigurationFactory instance by class '%s' failure", clazz).initCause(exception);
            }
        }
        for (ElasticJobProp prop : annotation.props()) {
            jobConfigurationBuilder.setProperty(prop.key(), prop.value());
        }
        return jobConfigurationBuilder.build();
    }
}
