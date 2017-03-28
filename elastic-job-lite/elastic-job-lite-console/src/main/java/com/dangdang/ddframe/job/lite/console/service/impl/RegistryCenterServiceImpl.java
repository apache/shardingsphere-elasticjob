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

import com.dangdang.ddframe.job.lite.console.domain.GlobalConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfigurations;
import com.dangdang.ddframe.job.lite.console.repository.ConfigurationsXmlRepository;
import com.dangdang.ddframe.job.lite.console.repository.impl.ConfigurationsXmlRepositoryImpl;
import com.dangdang.ddframe.job.lite.console.service.RegistryCenterService;
import com.google.common.base.Optional;

public class RegistryCenterServiceImpl implements RegistryCenterService {
    
    private ConfigurationsXmlRepository configurationsXmlRepository = new ConfigurationsXmlRepositoryImpl("RegistryCenterConfigurations.xml");
    
    @Override
    public GlobalConfiguration loadAll() {
        GlobalConfiguration globalConfiguration = configurationsXmlRepository.load();
        if (null == globalConfiguration.getRegistryCenterConfigurations()) {
            globalConfiguration.setRegistryCenterConfigurations(new RegistryCenterConfigurations());
        }
        return globalConfiguration;
    }
    
    @Override
    public RegistryCenterConfiguration load(final String name) {
        GlobalConfiguration configs = loadAll();
        RegistryCenterConfiguration result = findRegistryCenterConfiguration(name, configs);
        setActivated(configs, result);
        return result;
    }
    
    public RegistryCenterConfiguration findRegistryCenterConfiguration(final String name, final GlobalConfiguration configs) {
        for (RegistryCenterConfiguration each : configs.getRegistryCenterConfigurations().getRegistryCenterConfiguration()) {
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
    
    private void setActivated(final GlobalConfiguration configs, final RegistryCenterConfiguration toBeConnectedConfig) {
        RegistryCenterConfiguration activatedConfig = findActivatedRegistryCenterConfiguration(configs);
        if (!toBeConnectedConfig.equals(activatedConfig)) {
            if (null != activatedConfig) {
                activatedConfig.setActivated(false);
            }
            toBeConnectedConfig.setActivated(true);
            configurationsXmlRepository.save(configs);
        }
    }
    
    @Override
    public Optional<RegistryCenterConfiguration> loadActivated() {
        GlobalConfiguration configs = loadAll();
        RegistryCenterConfiguration result = findActivatedRegistryCenterConfiguration(configs);
        if (null == result) {
            return Optional.absent();
        }
        return Optional.of(result);
    }
    
    private RegistryCenterConfiguration findActivatedRegistryCenterConfiguration(final GlobalConfiguration configs) {
        for (RegistryCenterConfiguration each : configs.getRegistryCenterConfigurations().getRegistryCenterConfiguration()) {
            if (each.isActivated()) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public boolean add(final RegistryCenterConfiguration config) {
        GlobalConfiguration configs = loadAll();
        boolean result = configs.getRegistryCenterConfigurations().getRegistryCenterConfiguration().add(config);
        if (result) {
            configurationsXmlRepository.save(configs);
        }
        return result;
    }
    
    @Override
    public void delete(final String name) {
        GlobalConfiguration configs = loadAll();
        if (configs.getRegistryCenterConfigurations().getRegistryCenterConfiguration().remove(new RegistryCenterConfiguration(name))) {
            configurationsXmlRepository.save(configs);
        }
    }
}
