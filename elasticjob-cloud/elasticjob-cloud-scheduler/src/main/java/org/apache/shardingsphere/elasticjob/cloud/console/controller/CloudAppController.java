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

package org.apache.shardingsphere.elasticjob.cloud.console.controller;

import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Optional;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.exception.AppConfigurationException;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cloud app controller.
 */
@RestController
@RequestMapping("/app")
public final class CloudAppController {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static ProducerManager producerManager;
    
    private final CloudAppConfigurationService appConfigService;
    
    private final CloudJobConfigurationService jobConfigService;
    
    private final DisableAppService disableAppService;
    
    private final MesosStateService mesosStateService;
    
    public CloudAppController() {
        appConfigService = new CloudAppConfigurationService(regCenter);
        jobConfigService = new CloudJobConfigurationService(regCenter);
        mesosStateService = new MesosStateService(regCenter);
        disableAppService = new DisableAppService(regCenter);
    }
    
    /**
     * Init.
     *
     * @param producerManager producer manager
     * @param regCenter registry center
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        CloudAppController.regCenter = regCenter;
        CloudAppController.producerManager = producerManager;
    }
    
    /**
     * Register app config.
     *
     * @param appConfig cloud app config
     */
    @PostMapping
    public void register(@RequestBody final CloudAppConfigurationPOJO appConfig) {
        Optional<CloudAppConfigurationPOJO> appConfigFromZk = appConfigService.load(appConfig.getAppName());
        if (appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("app '%s' already existed.", appConfig.getAppName());
        }
        appConfigService.add(appConfig);
    }
    
    /**
     * Update app config.
     *
     * @param appConfig cloud app config
     */
    @PutMapping
    public void update(@RequestBody final CloudAppConfigurationPOJO appConfig) {
        appConfigService.update(appConfig);
    }
    
    /**
     * Query app config.
     *
     * @param appName app name
     * @return cloud app config
     */
    @GetMapping("/{appName}")
    public CloudAppConfigurationPOJO detail(@PathVariable("appName") final String appName) {
        Optional<CloudAppConfigurationPOJO> appConfig = appConfigService.load(appName);
        return appConfig.orElse(null);
    }
    
    /**
     * Find all registered app configs.
     *
     * @return collection of registered app configs
     */
    @GetMapping("/list")
    public Collection<CloudAppConfigurationPOJO> findAllApps() {
        return appConfigService.loadAll();
    }
    
    /**
     * Query the app is disabled or not.
     *
     * @param appName app name
     * @return true is disabled, otherwise not
     */
    @GetMapping("/{appName}/disable")
    public boolean isDisabled(@PathVariable("appName") final String appName) {
        return disableAppService.isDisabled(appName);
    }
    
    /**
     * Disable app config.
     *
     * @param appName app name
     */
    @PostMapping("/{appName}/disable")
    public void disable(@PathVariable("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.add(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.unschedule(each.getJobName());
                }
            }
        }
    }
    
    /**
     * Enable app.
     *
     * @param appName app name
     */
    @PostMapping("/{appName}/enable")
    public void enable(@PathVariable("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.remove(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.reschedule(each.getJobName());
                }
            }
        }
    }
    
    /**
     * Deregister app.
     *
     * @param appName app name
     */
    @DeleteMapping("/{appName}")
    public void deregister(@PathVariable("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            removeAppAndJobConfigurations(appName);
            stopExecutors(appName);
        }
    }
    
    private void removeAppAndJobConfigurations(final String appName) {
        for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
            if (appName.equals(each.getAppName())) {
                producerManager.deregister(each.getJobName());
            }
        }
        disableAppService.remove(appName);
        appConfigService.remove(appName);
    }
    
    private void stopExecutors(final String appName) {
        try {
            Collection<ExecutorStateInfo> executorBriefInfo = mesosStateService.executors(appName);
            for (ExecutorStateInfo each : executorBriefInfo) {
                producerManager.sendFrameworkMessage(ExecutorID.newBuilder().setValue(each.getId()).build(),
                        SlaveID.newBuilder().setValue(each.getSlaveId()).build(), "STOP".getBytes());
            }
        } catch (final JsonParseException ex) {
            throw new JobSystemException(ex);
        }
    }
}
