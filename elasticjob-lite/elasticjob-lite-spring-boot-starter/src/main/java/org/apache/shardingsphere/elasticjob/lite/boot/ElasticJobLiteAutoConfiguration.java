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

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.config.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.boot.ElasticJobRegistryCenterAutoConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.assertj.core.util.Strings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Configuration
@AutoConfigureAfter(ElasticJobRegistryCenterAutoConfiguration.class)
@ConditionalOnProperty(name = "elasticjob.enabled", havingValue = "true", matchIfMissing = true)
@Import(ElasticJobStartupRunner.class)
@EnableConfigurationProperties(ElasticJobProperties.class)
public class ElasticJobLiteAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Create JobBootstrap instances.
     *
     * @throws ClassNotFoundException if the Class configured under classed jobs not found
     */
    @PostConstruct
    public void createJobBootstrapBeans() throws ClassNotFoundException {
        ElasticJobProperties elasticJobProperties = this.applicationContext.getBean(ElasticJobProperties.class);

        // Looking for a better way
        final SingletonBeanRegistry beanFactory = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory();

        final CoordinatorRegistryCenter registryCenter = this.applicationContext.getBean(CoordinatorRegistryCenter.class);
        TracingConfiguration<?> tracingConfiguration = null;
        Map<String, TracingConfiguration> tracingConfigurationBeans = this.applicationContext.getBeansOfType(TracingConfiguration.class);
        if (tracingConfigurationBeans.size() == 1) {
            tracingConfiguration = tracingConfigurationBeans.values().stream().findAny().orElse(null);
        } else if (tracingConfigurationBeans.size() > 1) {
            throw new BeanCreationException(
                    "More than one [org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration] beans found. "
                            + "Consider disabling [org.apache.shardingsphere.elasticjob.tracing.boot.ElasticjobTracingAutoConfiguration].");
        }

        for (String elasticJobClassName : elasticJobProperties.getClassed().keySet()) {
            List<JobConfigurationPOJO> jobConfigurationPojoList = elasticJobProperties.getClassed().get(elasticJobClassName);
            for (JobConfigurationPOJO jobConfigurationPojo : jobConfigurationPojoList) {
                JobConfiguration jobConfiguration = jobConfigurationPojo.toJobConfiguration();
                ElasticJob elasticJob = (ElasticJob) applicationContext.getBean(
                        Thread.currentThread().getContextClassLoader().loadClass(elasticJobClassName));
                if (!Strings.isNullOrEmpty(jobConfiguration.getCron())) {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "ScheduleJobBootstrap",
                            new ScheduleJobBootstrap(registryCenter,
                                    elasticJob,
                                    jobConfiguration, tracingConfiguration));
                } else {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "OneOffJobBootstrap",
                            new OneOffJobBootstrap(registryCenter,
                                    elasticJob,
                                    jobConfiguration, tracingConfiguration));
                }
            }
        }
        for (String elasticJobType : elasticJobProperties.getTyped().keySet()) {
            List<JobConfigurationPOJO> jobConfigurationPojoList = elasticJobProperties.getTyped().get(elasticJobType);
            for (JobConfigurationPOJO jobConfigurationPojo : jobConfigurationPojoList) {
                JobConfiguration jobConfiguration = jobConfigurationPojo.toJobConfiguration();
                if (!Strings.isNullOrEmpty(jobConfiguration.getCron())) {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "ScheduleJobBootstrap",
                            new ScheduleJobBootstrap(registryCenter,
                                    elasticJobType,
                                    jobConfiguration, tracingConfiguration));
                } else {
                    beanFactory.registerSingleton(
                            jobConfiguration.getJobName() + "OneOffJobBootstrap",
                            new OneOffJobBootstrap(registryCenter,
                                    elasticJobType,
                                    jobConfiguration, tracingConfiguration));
                }
            }
        }
    }
}
