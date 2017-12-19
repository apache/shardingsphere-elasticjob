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

package io.elasticjob.lite.console.service;

import io.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import io.elasticjob.lite.console.domain.RegistryCenterConfigurations;
import com.google.common.base.Optional;

/**
 * 注册中心配置服务.
 *
 * @author zhangliang
 */
public interface RegistryCenterConfigurationService {
    
    /**
     * 读取全部注册中心配置.
     *
     * @return 全部注册中心配置
     */
    RegistryCenterConfigurations loadAll();
    
    /**
     * 读取注册中心配置.
     *
     * @param name 配置名称
     * @return 注册中心配置
     */
    RegistryCenterConfiguration load(String name);
    
    /**
     * 查找注册中心配置.
     * 
     * @param name 配置名称
     * @param configs 全部注册中心配置
     * @return 注册中心配置
     */
    RegistryCenterConfiguration find(final String name, final RegistryCenterConfigurations configs);
    
    /**
     * 读取已连接的注册中心配置.
     *
     * @return 已连接的注册中心配置
     */
    Optional<RegistryCenterConfiguration> loadActivated();
    
    /**
     * 添加注册中心配置.
     *
     * @param config 注册中心配置
     * @return 是否添加成功
     */
    boolean add(RegistryCenterConfiguration config);
    
    /**
     * 删除注册中心配置.
     *
     * @param name 配置名称
     */
    void delete(String name);
}
