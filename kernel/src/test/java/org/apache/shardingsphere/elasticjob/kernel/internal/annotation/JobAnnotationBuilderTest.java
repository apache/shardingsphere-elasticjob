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

package org.apache.shardingsphere.elasticjob.kernel.internal.annotation;

import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfigurationFactory;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.kernel.internal.annotation.fixture.AnnotationJobFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobAnnotationBuilderTest {
    
    @Test
    void assertGenerateJobConfiguration() {
        JobConfiguration jobConfig = JobAnnotationBuilder.generateJobConfiguration(AnnotationJobFixture.class);
        assertThat(jobConfig.getJobName(), is("AnnotationJobFixture"));
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        assertThat(jobConfig.getShardingItemParameters(), is("0=a,1=b,2=c"));
        assertThat(jobConfig.getCron(), is("*/10 * * * * ?"));
        assertTrue(jobConfig.isMonitorExecution());
        assertFalse(jobConfig.isFailover());
        assertTrue(jobConfig.isMisfire());
        assertThat(jobConfig.getMaxTimeDiffSeconds(), is(-1));
        assertThat(jobConfig.getReconcileIntervalMinutes(), is(10));
        assertNull(jobConfig.getJobShardingStrategyType());
        assertNull(jobConfig.getJobExecutorThreadPoolSizeProviderType());
        assertNull(jobConfig.getJobErrorHandlerType());
        assertThat(jobConfig.getDescription(), is("desc"));
        assertThat(jobConfig.getProps().getProperty("print.title"), is("test title"));
        assertThat(jobConfig.getProps().getProperty("print.content"), is("test content"));
        assertFalse(jobConfig.isDisabled());
        assertFalse(jobConfig.isOverwrite());
        assertNull(jobConfig.getLabel());
        assertFalse(jobConfig.isStaticSharding());
    }
    
    @Test
    void assertGenerateJobConfigurationWithFullAttributes() {
        JobConfiguration jobConfig = JobAnnotationBuilder.generateJobConfiguration(FullAnnotationJob.class);
        assertThat(jobConfig.getJobName(), is("fullAnnotationJob"));
        assertThat(jobConfig.getCron(), is("0/5 * * * * ?"));
        assertThat(jobConfig.getTimeZone(), is("GMT+08:00"));
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        assertThat(jobConfig.getShardingItemParameters(), is("0=Beijing,1=Shanghai,2=Guangzhou"));
        assertThat(jobConfig.getJobParameter(), is("param"));
        assertFalse(jobConfig.isMonitorExecution());
        assertTrue(jobConfig.isFailover());
        assertFalse(jobConfig.isMisfire());
        assertThat(jobConfig.getMaxTimeDiffSeconds(), is(100));
        assertThat(jobConfig.getReconcileIntervalMinutes(), is(5));
        assertThat(jobConfig.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(jobConfig.getJobExecutorThreadPoolSizeProviderType(), is("CPU"));
        assertThat(jobConfig.getJobErrorHandlerType(), is("LOG"));
        assertThat(jobConfig.getJobListenerTypes().size(), is(2));
        assertTrue(jobConfig.getJobListenerTypes().contains("NOOP"));
        assertTrue(jobConfig.getJobListenerTypes().contains("LOG"));
        assertThat(jobConfig.getExtraConfigurations().size(), is(1));
        assertThat(jobConfig.getDescription(), is("full desc"));
        assertThat(jobConfig.getProps().getProperty("key"), is("value"));
        assertTrue(jobConfig.isDisabled());
        assertTrue(jobConfig.isOverwrite());
        assertThat(jobConfig.getLabel(), is("test-label"));
        assertTrue(jobConfig.isStaticSharding());
    }
    
    @Test
    void assertGenerateJobConfigurationWithEmptyCronAndTimeZone() {
        JobConfiguration jobConfig = JobAnnotationBuilder.generateJobConfiguration(OneOffAnnotationJob.class);
        assertNull(jobConfig.getCron());
        assertNull(jobConfig.getTimeZone());
        assertNull(jobConfig.getJobShardingStrategyType());
        assertNull(jobConfig.getJobExecutorThreadPoolSizeProviderType());
        assertNull(jobConfig.getJobErrorHandlerType());
    }
    
    @Test
    void assertGenerateJobConfigurationWithSubclassFallback() {
        JobConfiguration jobConfig = JobAnnotationBuilder.generateJobConfiguration(SubAnnotationJob.class);
        assertThat(jobConfig.getJobName(), is("AnnotationJobFixture"));
    }
    
    @Test
    void assertGenerateJobConfigurationFailedWithMissingAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> JobAnnotationBuilder.generateJobConfiguration(NoAnnotationJob.class));
    }
    
    @Test
    void assertGenerateJobConfigurationFailedWithEmptyJobName() {
        assertThrows(IllegalArgumentException.class, () -> JobAnnotationBuilder.generateJobConfiguration(EmptyJobNameJob.class));
    }
    
    @Test
    void assertGenerateJobConfigurationFailedWithInvalidExtraConfigurationFactory() {
        assertThrows(JobConfigurationException.class, () -> JobAnnotationBuilder.generateJobConfiguration(InvalidExtraConfigJob.class));
    }
    
    @ElasticJobConfiguration(
            jobName = "fullAnnotationJob",
            cron = "0/5 * * * * ?",
            timeZone = "GMT+08:00",
            shardingTotalCount = 3,
            shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou",
            jobParameter = "param",
            monitorExecution = false,
            failover = true,
            misfire = false,
            maxTimeDiffSeconds = 100,
            reconcileIntervalMinutes = 5,
            jobShardingStrategyType = "AVG_ALLOCATION",
            jobExecutorThreadPoolSizeProviderType = "CPU",
            jobErrorHandlerType = "LOG",
            jobListenerTypes = {"NOOP", "LOG"},
            extraConfigurations = TestExtraConfigurationFactory.class,
            description = "full desc",
            props = @ElasticJobProp(key = "key", value = "value"),
            disabled = true,
            overwrite = true,
            label = "test-label",
            staticSharding = true)
    private static final class FullAnnotationJob {
    }
    
    @ElasticJobConfiguration(jobName = "oneOffJob", shardingTotalCount = 1)
    private static final class OneOffAnnotationJob {
    }
    
    private static final class SubAnnotationJob extends AnnotationJobFixture {
    }
    
    private static final class NoAnnotationJob {
    }
    
    @ElasticJobConfiguration(jobName = "", shardingTotalCount = 1)
    private static final class EmptyJobNameJob {
    }
    
    @ElasticJobConfiguration(jobName = "invalidExtraConfigJob", shardingTotalCount = 1, extraConfigurations = PrivateExtraConfigurationFactory.class)
    private static final class InvalidExtraConfigJob {
    }
    
    public static final class TestExtraConfigurationFactory implements JobExtraConfigurationFactory {
        
        @Override
        public Optional<JobExtraConfiguration> getJobExtraConfiguration() {
            return Optional.of(new JobExtraConfiguration() {
            });
        }
    }
    
    private static final class PrivateExtraConfigurationFactory implements JobExtraConfigurationFactory {
        
        private PrivateExtraConfigurationFactory() {
        }
        
        @Override
        public Optional<JobExtraConfiguration> getJobExtraConfiguration() {
            return Optional.empty();
        }
    }
}
