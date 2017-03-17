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

package com.dangdang.ddframe.job.lite.console.service.impl;

import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfigurations;
import com.dangdang.ddframe.job.lite.console.repository.RegistryCenterConfigurationsXmlRepository;
import com.dangdang.ddframe.job.lite.console.repository.impl.RegistryCenterConfigurationsXmlRepositoryImpl;
import com.dangdang.ddframe.job.lite.console.service.RegistryCenterService;
import com.google.common.base.Optional;

public class RegistryCenterServiceImpl implements RegistryCenterService {
    
    private RegistryCenterConfigurationsXmlRepository regCenterConfigurationsXmlRepository = new RegistryCenterConfigurationsXmlRepositoryImpl();
    
    @Override
    public RegistryCenterConfigurations loadAll() {
        return regCenterConfigurationsXmlRepository.load();
    }
    
    @Override
    public RegistryCenterConfiguration load(final String name) {
        RegistryCenterConfigurations configs = loadAll();
        RegistryCenterConfiguration result = findRegistryCenterConfiguration(name, configs);
        setActivated(configs, result);
        return result;
    }
    
    public RegistryCenterConfiguration findRegistryCenterConfiguration(final String name, final RegistryCenterConfigurations configs) {
        for (RegistryCenterConfiguration each : configs.getRegistryCenterConfiguration()) {
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
    
    private void setActivated(final RegistryCenterConfigurations configs, final RegistryCenterConfiguration toBeConnectedConfig) {
        RegistryCenterConfiguration activatedConfig = findActivatedRegistryCenterConfiguration(configs);
        if (!toBeConnectedConfig.equals(activatedConfig)) {
            if (null != activatedConfig) {
                activatedConfig.setActivated(false);
            }
            toBeConnectedConfig.setActivated(true);
            regCenterConfigurationsXmlRepository.save(configs);
        }
    }
    
    @Override
    public Optional<RegistryCenterConfiguration> loadActivated() {
        RegistryCenterConfigurations configs = loadAll();
        RegistryCenterConfiguration result = findActivatedRegistryCenterConfiguration(configs);
        if (null == result) {
            return Optional.absent();
        }
        return Optional.of(result);
    }
    
    private RegistryCenterConfiguration findActivatedRegistryCenterConfiguration(final RegistryCenterConfigurations configs) {
        for (RegistryCenterConfiguration each : configs.getRegistryCenterConfiguration()) {
            if (each.isActivated()) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public boolean add(final RegistryCenterConfiguration config) {
        RegistryCenterConfigurations configs = loadAll();
        boolean result = configs.getRegistryCenterConfiguration().add(config);
        if (result) {
            regCenterConfigurationsXmlRepository.save(configs);
        }
        return result;
    }
    
    @Override
    public void delete(final String name) {
        RegistryCenterConfigurations configs = loadAll();
        if (configs.getRegistryCenterConfiguration().remove(new RegistryCenterConfiguration(name))) {
            regCenterConfigurationsXmlRepository.save(configs);
        }
    }
}
