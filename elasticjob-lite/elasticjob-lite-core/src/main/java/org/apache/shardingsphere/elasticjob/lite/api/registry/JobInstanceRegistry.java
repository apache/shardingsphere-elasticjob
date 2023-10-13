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

package org.apache.shardingsphere.elasticjob.lite.api.registry;

import lombok.RequiredArgsConstructor;

import org.apache.curator.utils.ThreadUtils;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Job instance registry.
 */
@RequiredArgsConstructor
public final class JobInstanceRegistry {
    
    private static final Pattern JOB_CONFIG_COMPILE = Pattern.compile("/(\\w+)/config");
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final JobInstance jobInstance;
    
    /**
     * Register.
     */
    public void register() {
        ThreadFactory threadFactory = ThreadUtils.newGenericThreadFactory("ListenerNotify-instanceRegistry");
        Executor executor = Executors.newSingleThreadExecutor(threadFactory);
        regCenter.watch("/", new JobInstanceRegistryListener(), executor);
    }
    
    public class JobInstanceRegistryListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (event.getType() != DataChangedEvent.Type.ADDED || !isJobConfigPath(event.getKey())) {
                return;
            }
            JobConfiguration jobConfig = YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class).toJobConfiguration();
            if (jobConfig.isDisabled() || !isLabelMatch(jobConfig)) {
                return;
            }
            if (!jobConfig.getCron().isEmpty()) {
                new ScheduleJobBootstrap(regCenter, newElasticJobInstance(jobConfig), jobConfig).schedule();
            } else if (!isAllShardingItemsCompleted(jobConfig)) {
                new OneOffJobBootstrap(regCenter, newElasticJobInstance(jobConfig), jobConfig).execute();
            }
        }
        
        private boolean isAllShardingItemsCompleted(final JobConfiguration jobConfig) {
            JobNodePath jobNodePath = new JobNodePath(jobConfig.getJobName());
            return IntStream.range(0, jobConfig.getShardingTotalCount())
                    .allMatch(each -> regCenter.isExisted(jobNodePath.getShardingNodePath(String.valueOf(each), "completed")));
        }
        
        private ElasticJob newElasticJobInstance(final JobConfiguration jobConfig) {
            String clazz = regCenter.get(String.format("/%s", jobConfig.getJobName()));
            try {
                return (ElasticJob) Class.forName(clazz).newInstance();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new RuntimeException(String.format("new elastic job instance by class '%s' failure", clazz), ex);
            }
        }
        
        private boolean isLabelMatch(final JobConfiguration jobConfig) {
            if (jobConfig.getLabel() == null) {
                return false;
            }
            if (jobInstance.getLabels() == null) {
                return true;
            }
            return Arrays.stream(jobInstance.getLabels().split(",")).collect(Collectors.toSet()).contains(jobConfig.getLabel());
        }
        
        private boolean isJobConfigPath(final String path) {
            return JOB_CONFIG_COMPILE.matcher(path).matches();
        }
    }
}
