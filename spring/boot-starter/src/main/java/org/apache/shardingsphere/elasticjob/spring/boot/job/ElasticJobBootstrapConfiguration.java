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

package org.apache.shardingsphere.elasticjob.spring.boot.job;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.spring.boot.tracing.TracingProperties;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Job bootstrap configuration.
 */
@Slf4j
public class ElasticJobBootstrapConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    @Setter
    private ApplicationContext applicationContext;
    
    @Override
    public void afterSingletonsInstantiated() {
        log.info("creating Job Bootstrap Beans");
        createJobBootstrapBeans();
        log.info("Job Bootstrap Beans created.");
    }
    
    /**
     * Create job bootstrap instances and register them into container.
     */
    public void createJobBootstrapBeans() {
        ElasticJobProperties elasticJobProperties = applicationContext.getBean(ElasticJobProperties.class);
        SingletonBeanRegistry singletonBeanRegistry = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        CoordinatorRegistryCenter registryCenter = applicationContext.getBean(CoordinatorRegistryCenter.class);
        TracingConfiguration<?> tracingConfig = getTracingConfiguration();
        constructJobBootstraps(elasticJobProperties, singletonBeanRegistry, registryCenter, tracingConfig);
    }
    
    @SuppressWarnings("rawtypes")
    private TracingConfiguration<?> getTracingConfiguration() {
        Map<String, TracingConfiguration> tracingConfigurationBeans = applicationContext.getBeansOfType(TracingConfiguration.class);
        if (tracingConfigurationBeans.isEmpty()) {
            return null;
        }
        if (1 == tracingConfigurationBeans.size()) {
            return tracingConfigurationBeans.values().iterator().next();
        }
        throw new BeanCreationException("More than one [org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration] beans found. "
                + "Consider disabling [org.apache.shardingsphere.elasticjob.tracing.boot.ElasticJobTracingAutoConfiguration].");
    }
    
    private void constructJobBootstraps(final ElasticJobProperties elasticJobProperties, final SingletonBeanRegistry singletonBeanRegistry,
                                        final CoordinatorRegistryCenter registryCenter, final TracingConfiguration<?> tracingConfig) {
        for (Map.Entry<String, ElasticJobConfigurationProperties> entry : elasticJobProperties.getJobs().entrySet()) {
            ElasticJobConfigurationProperties jobConfigurationProperties = entry.getValue();
            Preconditions.checkArgument(null != jobConfigurationProperties.getElasticJobClass()
                    || !Strings.isNullOrEmpty(jobConfigurationProperties.getElasticJobType()),
                    "Please specific [elasticJobClass] or [elasticJobType] under job configuration.");
            Preconditions.checkArgument(null == jobConfigurationProperties.getElasticJobClass()
                    || Strings.isNullOrEmpty(jobConfigurationProperties.getElasticJobType()),
                    "[elasticJobClass] and [elasticJobType] are mutually exclusive.");
            if (null != jobConfigurationProperties.getElasticJobClass()) {
                registerClassedJob(entry.getKey(), entry.getValue().getJobBootstrapBeanName(), singletonBeanRegistry, registryCenter, tracingConfig, jobConfigurationProperties);
            } else if (!Strings.isNullOrEmpty(jobConfigurationProperties.getElasticJobType())) {
                registerTypedJob(entry.getKey(), entry.getValue().getJobBootstrapBeanName(), singletonBeanRegistry, registryCenter, tracingConfig, jobConfigurationProperties);
            }
        }
    }
    
    private void registerClassedJob(final String jobName, final String jobBootstrapBeanName, final SingletonBeanRegistry singletonBeanRegistry, final CoordinatorRegistryCenter registryCenter,
                                    final TracingConfiguration<?> tracingConfig, final ElasticJobConfigurationProperties jobConfigurationProperties) {
        JobConfiguration jobConfig = jobConfigurationProperties.toJobConfiguration(jobName);
        jobExtraConfigurations(jobConfig, tracingConfig);
        ElasticJob elasticJob = applicationContext.getBean(jobConfigurationProperties.getElasticJobClass());
        if (Strings.isNullOrEmpty(jobConfig.getCron())) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobBootstrapBeanName), "The property [jobBootstrapBeanName] is required for One-off job.");
            singletonBeanRegistry.registerSingleton(jobBootstrapBeanName, new OneOffJobBootstrap(registryCenter, elasticJob, jobConfig));
        } else {
            String beanName = !Strings.isNullOrEmpty(jobBootstrapBeanName) ? jobBootstrapBeanName : jobConfig.getJobName() + "ScheduleJobBootstrap";
            singletonBeanRegistry.registerSingleton(beanName, new ScheduleJobBootstrap(registryCenter, elasticJob, jobConfig));
        }
    }
    
    private void registerTypedJob(final String jobName, final String jobBootstrapBeanName, final SingletonBeanRegistry singletonBeanRegistry, final CoordinatorRegistryCenter registryCenter,
                                  final TracingConfiguration<?> tracingConfig, final ElasticJobConfigurationProperties jobConfigurationProperties) {
        JobConfiguration jobConfig = jobConfigurationProperties.toJobConfiguration(jobName);
        jobExtraConfigurations(jobConfig, tracingConfig);
        if (Strings.isNullOrEmpty(jobConfig.getCron())) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobBootstrapBeanName), "The property [jobBootstrapBeanName] is required for One-off job.");
            singletonBeanRegistry.registerSingleton(jobBootstrapBeanName, new OneOffJobBootstrap(registryCenter, jobConfigurationProperties.getElasticJobType(), jobConfig));
        } else {
            String beanName = !Strings.isNullOrEmpty(jobBootstrapBeanName) ? jobBootstrapBeanName : jobConfig.getJobName() + "ScheduleJobBootstrap";
            singletonBeanRegistry.registerSingleton(beanName, new ScheduleJobBootstrap(registryCenter, jobConfigurationProperties.getElasticJobType(), jobConfig));
        }
    }
    
    private void jobExtraConfigurations(final JobConfiguration jobConfig, final TracingConfiguration<?> tracingConfig) {
        if (null == tracingConfig) {
            return;
        }
        TracingProperties tracingProperties = applicationContext.getBean(TracingProperties.class);
        Preconditions.checkArgument(tracingProperties.getIncludeJobNames().isEmpty() || tracingProperties.getExcludeJobNames().isEmpty(),
                "[tracing.includeJobNames] and [tracing.excludeJobNames] are mutually exclusive.");
        if ((tracingProperties.getIncludeJobNames().isEmpty() || tracingProperties.getIncludeJobNames().contains(jobConfig.getJobName()))
                && !tracingProperties.getExcludeJobNames().contains(jobConfig.getJobName())) {
            jobConfig.getExtraConfigurations().add(tracingConfig);
        }
    }
}
