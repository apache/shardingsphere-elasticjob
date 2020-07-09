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

package org.apache.shardingsphere.elasticjob.lite.springboot.autoconfigure;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.job.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.ElasticJobProperty;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.RegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.Tracing;
import org.apache.shardingsphere.elasticjob.lite.springboot.hook.ElasticJobStartUpRunner;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(ElasticJobLiteProperties.class)
@ConditionalOnProperty(name = "elasticjob.enabled", matchIfMissing = true)
@Import(ElasticJobStartUpRunner.class)
public class ElasticJobLiteAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * A default RegistryCenter.
     *
     * @param elasticJobLiteProperties ConfigurationProperties
     * @return ZookeeperRegistryCenter
     */
    @ConditionalOnProperty({"elasticjob.registryCenter.zookeeper.serverLists", "elasticjob.registryCenter.zookeeper.namespace"})
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter(final ElasticJobLiteProperties elasticJobLiteProperties) {
        ZookeeperConfigBean configBean = elasticJobLiteProperties.getRegistryCenter().getZookeeper();
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(configBean.getServerLists(), configBean.getNamespace());
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(configBean.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(configBean.getConnectionTimeoutMilliseconds());
        zookeeperConfiguration.setMaxRetries(configBean.getMaxRetries());
        zookeeperConfiguration.setSessionTimeoutMilliseconds(configBean.getSessionTimeoutMilliseconds());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(configBean.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setDigest(configBean.getDigest());
        return new ZookeeperRegistryCenter(zookeeperConfiguration);
    }


    /**
     * Create a default TracingConfiguration.
     *
     * @param dataSource DataSource for Tracing
     * @return TracingConfiguration
     */
    @ConditionalOnProperty(value = "elasticjob.tracing.type", havingValue = "RDB")
    @Bean
    public TracingConfiguration tracingConfiguration(final DataSource dataSource) {
        return new TracingConfiguration<>("RDB", dataSource);
    }

    /**
     * Create JobBootstrap beans.
     */
    @PostConstruct
    public void constructJobBootstraps() {
        List<ElasticJobConfigHolder> jobDefinitions = new ArrayList<>();
        jobDefinitions.addAll(configureJobByAnnotations());
        jobDefinitions.addAll(configureJobByProperties());

        // Looking for a better way
        SingletonBeanRegistry beanFactory = ((AnnotationConfigApplicationContext) this.applicationContext).getBeanFactory();

        jobDefinitions.forEach(definition -> {
            switch (definition.getJobBootstrapType()) {
                case SCHEDULE:
                    ScheduleJobBootstrap scheduleJobBootstrap = null;
                    if (definition.getInstance() != null) {
                        if (definition.getTracingConfiguration() != null) {
                            scheduleJobBootstrap = new ScheduleJobBootstrap(definition.getRegistryCenter(), definition.getInstance(),
                                    definition.getJobConfiguration(), definition.getTracingConfiguration());
                        } else {
                            scheduleJobBootstrap = new ScheduleJobBootstrap(definition.getRegistryCenter(), definition.getInstance(),
                                    definition.getJobConfiguration());
                        }
                    } else if (!Strings.isNullOrEmpty(definition.getElasticJobType())) {
                        if (definition.getTracingConfiguration() != null) {
                            scheduleJobBootstrap = new ScheduleJobBootstrap(definition.getRegistryCenter(), definition.getElasticJobType(),
                                    definition.getJobConfiguration(), definition.getTracingConfiguration());
                        } else {
                            scheduleJobBootstrap = new ScheduleJobBootstrap(definition.getRegistryCenter(), definition.getElasticJobType(),
                                    definition.getJobConfiguration());
                        }
                    }
                    String scheduleBeanName = definition.getJobConfiguration().getJobName() + "Schedule";
                    if (scheduleJobBootstrap != null) {
                        beanFactory.registerSingleton(scheduleBeanName, scheduleJobBootstrap);
                    }
                    break;

                case ONE_OFF:
                    OneOffJobBootstrap oneOffJobBootstrap = null;
                    if (definition.getInstance() != null) {
                        if (definition.getTracingConfiguration() != null) {
                            oneOffJobBootstrap = new OneOffJobBootstrap(definition.getRegistryCenter(), definition.getInstance(),
                                    definition.getJobConfiguration(), definition.getTracingConfiguration());
                        } else {
                            oneOffJobBootstrap = new OneOffJobBootstrap(definition.getRegistryCenter(), definition.getInstance(),
                                    definition.getJobConfiguration());
                        }
                    } else if (!Strings.isNullOrEmpty(definition.getElasticJobType())) {
                        if (definition.getTracingConfiguration() != null) {
                            oneOffJobBootstrap = new OneOffJobBootstrap(definition.getRegistryCenter(), definition.getElasticJobType(),
                                    definition.getJobConfiguration(), definition.getTracingConfiguration());
                        } else {
                            oneOffJobBootstrap = new OneOffJobBootstrap(definition.getRegistryCenter(), definition.getElasticJobType(),
                                    definition.getJobConfiguration());
                        }
                    }
                    String oneOffBeanName = definition.getJobConfiguration().getJobName() + "OneOff";
                    if (oneOffJobBootstrap != null) {
                        beanFactory.registerSingleton(oneOffBeanName, oneOffJobBootstrap);
                    }
                    break;

                default:
            }
        });
    }

    private List<ElasticJobConfigHolder> configureJobByAnnotations() {
        Map<String, Object> elasticjobs = this.applicationContext.getBeansWithAnnotation(ElasticJob.class);
        return elasticjobs.keySet().stream().map(beanName -> {
            ElasticJob elasticJob = this.applicationContext.findAnnotationOnBean(beanName, ElasticJob.class);
            ElasticJobConfigHolder holder = new ElasticJobConfigHolder();
            holder.setInstance((org.apache.shardingsphere.elasticjob.lite.api.job.ElasticJob) elasticjobs.get(beanName));
            holder.setJobBootstrapType(Objects.requireNonNull(elasticJob).jobBootstrapType());

            JobConfiguration.Builder builder = JobConfiguration.newBuilder(elasticJob.jobName(), elasticJob.shardingTotalCount())
                    .cron(elasticJob.cron())
                    .description(elasticJob.description())
                    .disabled(elasticJob.disabled())
                    .failover(elasticJob.failover())
                    .jobErrorHandlerType(elasticJob.jobErrorHandlerType())
                    .jobExecutorServiceHandlerType(elasticJob.jobExecutorServiceHandlerType())
                    .jobParameter(elasticJob.jobParameter())
                    .jobShardingStrategyType(elasticJob.jobShardingStrategyType())
                    .maxTimeDiffSeconds(elasticJob.maxTimeDiffSeconds())
                    .misfire(elasticJob.misfire())
                    .monitorExecution(elasticJob.monitorExecution())
                    .overwrite(elasticJob.overwrite())
                    .reconcileIntervalMinutes(elasticJob.reconcileIntervalMinutes())
                    .shardingItemParameters(elasticJob.shardingItemParameters());
            for (int i = 0; i < elasticJob.props().length; i++) {
                ElasticJobProperty property = elasticJob.props()[i];
                builder.setProperty(property.name(), property.value());
            }
            holder.setJobConfiguration(builder.build());

            RegistryCenter registryCenter = this.applicationContext.findAnnotationOnBean(beanName, RegistryCenter.class);
            if (registryCenter != null && !Strings.isNullOrEmpty(registryCenter.value())) {
                holder.setRegistryCenter(this.applicationContext.getBean(registryCenter.value(), CoordinatorRegistryCenter.class));
            } else {
                holder.setRegistryCenter(this.applicationContext.getBean(CoordinatorRegistryCenter.class));
            }

            Tracing tracing = this.applicationContext.findAnnotationOnBean(beanName, Tracing.class);
            if (tracing != null) {
                if (!Strings.isNullOrEmpty(tracing.value())) {
                    holder.setTracingConfiguration(this.applicationContext.getBean(tracing.value(), TracingConfiguration.class));
                } else {
                    holder.setTracingConfiguration(this.applicationContext.getBean(TracingConfiguration.class));
                }
            }
            return holder;
        }).collect(Collectors.toList());
    }

    /**
     * TODO configure Job By Properties.
     *
     * @return ElasticJobConfigHolder
     */
    private List<ElasticJobConfigHolder> configureJobByProperties() {
        return Collections.emptyList();
    }
}
