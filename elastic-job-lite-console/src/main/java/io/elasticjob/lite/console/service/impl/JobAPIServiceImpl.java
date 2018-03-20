/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.console.service.impl;

import io.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import io.elasticjob.lite.console.service.JobAPIService;
import io.elasticjob.lite.console.util.SessionRegistryCenterConfiguration;
import io.elasticjob.lite.lifecycle.api.JobAPIFactory;
import io.elasticjob.lite.lifecycle.api.JobOperateAPI;
import io.elasticjob.lite.lifecycle.api.JobSettingsAPI;
import io.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import io.elasticjob.lite.lifecycle.api.ServerStatisticsAPI;
import io.elasticjob.lite.lifecycle.api.ShardingOperateAPI;
import io.elasticjob.lite.lifecycle.api.ShardingStatisticsAPI;
import com.google.common.base.Optional;

/**
 * 作业API服务实现类.
 * 
 * @author zhangliang 
 */
public final class JobAPIServiceImpl implements JobAPIService {
    
    @Override
    public JobSettingsAPI getJobSettingsAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createJobSettingsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
    @Override
    public JobOperateAPI getJobOperatorAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createJobOperateAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
    @Override
    public ShardingOperateAPI getShardingOperateAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createShardingOperateAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
    @Override
    public JobStatisticsAPI getJobStatisticsAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createJobStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
    @Override
    public ServerStatisticsAPI getServerStatisticsAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createServerStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
    @Override
    public ShardingStatisticsAPI getShardingStatisticsAPI() {
        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        return JobAPIFactory.createShardingStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
    }
    
}
