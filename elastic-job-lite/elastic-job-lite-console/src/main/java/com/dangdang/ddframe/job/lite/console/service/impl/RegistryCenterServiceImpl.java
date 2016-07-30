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
import com.dangdang.ddframe.job.lite.console.service.RegistryCenterService;
import com.google.common.base.Optional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

@Service
public class RegistryCenterServiceImpl implements RegistryCenterService {
    
    @Resource
    private RegistryCenterConfigurationsXmlRepository regCenterConfigurationsXmlRepository;
    
    @Override
    public Collection<RegistryCenterConfiguration> loadAll() {
        return regCenterConfigurationsXmlRepository.load().getRegistryCenterConfiguration();
    }
    
    @Override
    public RegistryCenterConfiguration load(final String name) {
        RegistryCenterConfigurations configs = regCenterConfigurationsXmlRepository.load();
        RegistryCenterConfiguration result = findRegistryCenterConfiguration(name, configs);
        setActivated(configs, result);
        return result;
    }
    
    private RegistryCenterConfiguration findRegistryCenterConfiguration(final String name, final RegistryCenterConfigurations configs) {
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
        RegistryCenterConfigurations configs = regCenterConfigurationsXmlRepository.load();
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
        RegistryCenterConfigurations configs = regCenterConfigurationsXmlRepository.load();
        boolean result = configs.getRegistryCenterConfiguration().add(config);
        if (result) {
            regCenterConfigurationsXmlRepository.save(configs);
        }
        return result;
    }
    
    @Override
    public void delete(final String name) {
        RegistryCenterConfigurations configs = regCenterConfigurationsXmlRepository.load();
        if (configs.getRegistryCenterConfiguration().remove(new RegistryCenterConfiguration(name))) {
            regCenterConfigurationsXmlRepository.save(configs);
        }
    }
}
