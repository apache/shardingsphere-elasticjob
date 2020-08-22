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
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.ContextPath;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;

import java.util.Collection;
import java.util.Optional;

/**
 * Cloud app controller.
 */
@ContextPath("/api/app")
public final class CloudAppController implements RestfulController {
    
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
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST)
    public boolean register(@RequestBody final CloudAppConfigurationPOJO appConfig) {
        Optional<CloudAppConfigurationPOJO> appConfigFromZk = appConfigService.load(appConfig.getAppName());
        if (appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("app '%s' already existed.", appConfig.getAppName());
        }
        appConfigService.add(appConfig);
        return true;
    }
    
    /**
     * Update app config.
     *
     * @param appConfig cloud app config
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.PUT)
    public boolean update(@RequestBody final CloudAppConfigurationPOJO appConfig) {
        appConfigService.update(appConfig);
        return true;
    }
    
    /**
     * Query app config.
     *
     * @param appName app name
     * @return cloud app config
     */
    @Mapping(method = Http.GET, path = "/{appName}")
    public CloudAppConfigurationPOJO detail(@Param(name = "appName", source = ParamSource.PATH) final String appName) {
        Optional<CloudAppConfigurationPOJO> appConfig = appConfigService.load(appName);
        return appConfig.orElse(null);
    }
    
    /**
     * Find all registered app configs.
     *
     * @return collection of registered app configs
     */
    @Mapping(method = Http.GET, path = "/list")
    public Collection<CloudAppConfigurationPOJO> findAllApps() {
        return appConfigService.loadAll();
    }
    
    /**
     * Query the app is disabled or not.
     *
     * @param appName app name
     * @return true is disabled, otherwise not
     */
    @Mapping(method = Http.GET, path = "/{appName}/disable")
    public boolean isDisabled(@Param(name = "appName", source = ParamSource.PATH) final String appName) {
        return disableAppService.isDisabled(appName);
    }
    
    /**
     * Disable app config.
     *
     * @param appName app name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/{appName}/disable")
    public boolean disable(@Param(name = "appName", source = ParamSource.PATH) final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.add(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.unschedule(each.getJobName());
                }
            }
        }
        return true;
    }
    
    /**
     * Enable app.
     *
     * @param appName app name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/{appName}/enable")
    public boolean enable(@Param(name = "appName", source = ParamSource.PATH) final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.remove(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.reschedule(each.getJobName());
                }
            }
        }
        return true;
    }
    
    /**
     * Deregister app.
     *
     * @param appName app name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.DELETE, path = "/{appName}")
    public boolean deregister(@Param(name = "appName", source = ParamSource.PATH) final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            removeAppAndJobConfigurations(appName);
            stopExecutors(appName);
        }
        return true;
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
