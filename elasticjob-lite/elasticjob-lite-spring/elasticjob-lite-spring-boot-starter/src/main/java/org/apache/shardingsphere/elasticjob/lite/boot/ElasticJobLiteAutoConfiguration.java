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

package org.apache.shardingsphere.elasticjob.lite.boot;

import com.google.common.base.Strings;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.config.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.boot.ElasticJobRegistryCenterAutoConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Configuration
@AutoConfigureAfter(ElasticJobRegistryCenterAutoConfiguration.class)
@ConditionalOnProperty(name = "elasticjob.enabled", havingValue = "true", matchIfMissing = true)
@Import(ElasticJobStartupRunner.class)
@EnableConfigurationProperties(ElasticJobProperties.class)
@Setter
public class ElasticJobLiteAutoConfiguration implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    /**
     * Create job bootstrap instances.
     *
     * @throws ClassNotFoundException if the class configured under classed jobs not found
     */
    @PostConstruct
    public void createJobBootstrapBeans() throws ClassNotFoundException {
        ElasticJobProperties elasticJobProperties = applicationContext.getBean(ElasticJobProperties.class);
        // TODO Looking for a better way
        SingletonBeanRegistry beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        CoordinatorRegistryCenter registryCenter = applicationContext.getBean(CoordinatorRegistryCenter.class);
        TracingConfiguration tracingConfiguration = getTracingConfiguration();
        registerClassedJobs(elasticJobProperties, beanFactory, registryCenter, tracingConfiguration);
        registerTypedJobs(elasticJobProperties, beanFactory, registryCenter, tracingConfiguration);
    }
    
    private TracingConfiguration getTracingConfiguration() {
        Map<String, TracingConfiguration> tracingConfigurationBeans = applicationContext.getBeansOfType(TracingConfiguration.class);
        if (tracingConfigurationBeans.isEmpty()) {
            return null;
        }
        if (1 == tracingConfigurationBeans.size()) {
            return tracingConfigurationBeans.values().iterator().next();
        }
        throw new BeanCreationException(
                "More than one [org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration] beans found. "
                        + "Consider disabling [org.apache.shardingsphere.elasticjob.tracing.boot.ElasticJobTracingAutoConfiguration].");
    }
    
    private void registerClassedJobs(final ElasticJobProperties elasticJobProperties, final SingletonBeanRegistry beanFactory,
                                     final CoordinatorRegistryCenter registryCenter, final TracingConfiguration tracingConfiguration) throws ClassNotFoundException {
        for (Entry<String, List<JobConfigurationPOJO>> entry : elasticJobProperties.getClassed().entrySet()) {
            for (JobConfigurationPOJO each : entry.getValue()) {
                JobConfiguration jobConfiguration = each.toJobConfiguration();
                ElasticJob elasticJob = (ElasticJob) applicationContext.getBean(Thread.currentThread().getContextClassLoader().loadClass(entry.getKey()));
                if (Strings.isNullOrEmpty(jobConfiguration.getCron())) {
                    beanFactory.registerSingleton(jobConfiguration.getJobName() + "OneOffJobBootstrap", new OneOffJobBootstrap(registryCenter, elasticJob, jobConfiguration, tracingConfiguration));
                } else {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "ScheduleJobBootstrap", new ScheduleJobBootstrap(registryCenter, elasticJob, jobConfiguration, tracingConfiguration));
                }
            }
        }
    }
    
    private void registerTypedJobs(final ElasticJobProperties elasticJobProperties, final SingletonBeanRegistry beanFactory, 
                                   final CoordinatorRegistryCenter registryCenter, final TracingConfiguration tracingConfiguration) {
        for (Entry<String, List<JobConfigurationPOJO>> entry : elasticJobProperties.getTyped().entrySet()) {
            for (JobConfigurationPOJO each : entry.getValue()) {
                JobConfiguration jobConfiguration = each.toJobConfiguration();
                if (Strings.isNullOrEmpty(jobConfiguration.getCron())) {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "OneOffJobBootstrap", new OneOffJobBootstrap(registryCenter, entry.getKey(), jobConfiguration, tracingConfiguration));
                } else {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "ScheduleJobBootstrap", new ScheduleJobBootstrap(registryCenter, entry.getKey(), jobConfiguration, tracingConfiguration));
                }
            }
        }
    }
}
