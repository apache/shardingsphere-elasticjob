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

package org.apache.shardingsphere.elasticjob.lite.console.service.impl;

import org.apache.shardingsphere.elasticjob.lite.console.config.DynamicDataSourceConfig.DynamicDataSource;
import org.apache.shardingsphere.elasticjob.lite.console.config.DynamicDataSourceConfig.DynamicDataSourceContextHolder;
import org.apache.shardingsphere.elasticjob.lite.console.domain.DataSourceFactory;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfigurations;
import org.apache.shardingsphere.elasticjob.lite.console.domain.GlobalConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.repository.ConfigurationsXmlRepository;
import org.apache.shardingsphere.elasticjob.lite.console.repository.impl.ConfigurationsXmlRepositoryImpl;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceDataSourceConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Event trace data source configuration service implementation.
 */
@Service
public final class EventTraceDataSourceConfigurationServiceImpl implements EventTraceDataSourceConfigurationService {
    
    private ConfigurationsXmlRepository configurationsXmlRepository = new ConfigurationsXmlRepositoryImpl();
    
    @Autowired
    private DynamicDataSource dynamicDataSource;
    
    @Override
    public EventTraceDataSourceConfigurations loadAll() {
        return loadGlobal().getEventTraceDataSourceConfigurations();
    }
    
    @Override
    public EventTraceDataSourceConfiguration load(final String name) {
        GlobalConfiguration configs = loadGlobal();
        EventTraceDataSourceConfiguration result = find(name, configs.getEventTraceDataSourceConfigurations());
        setActivated(configs, result);
        // Activate the dataSource by data source name for spring boot
        DynamicDataSourceContextHolder.setDataSourceName(name);
        return result;
    }
    
    @Override
    public EventTraceDataSourceConfiguration find(final String name, final EventTraceDataSourceConfigurations configs) {
        for (EventTraceDataSourceConfiguration each : configs.getEventTraceDataSourceConfiguration()) {
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
    
    private void setActivated(final GlobalConfiguration configs, final EventTraceDataSourceConfiguration toBeConnectedConfig) {
        EventTraceDataSourceConfiguration activatedConfig = findActivatedDataSourceConfiguration(configs);
        if (!toBeConnectedConfig.equals(activatedConfig)) {
            if (null != activatedConfig) {
                activatedConfig.setActivated(false);
            }
            toBeConnectedConfig.setActivated(true);
            configurationsXmlRepository.save(configs);
        }
    }
    
    @Override
    public Optional<EventTraceDataSourceConfiguration> loadActivated() {
        return Optional.ofNullable(findActivatedDataSourceConfiguration(loadGlobal()));
    }
    
    private EventTraceDataSourceConfiguration findActivatedDataSourceConfiguration(final GlobalConfiguration configs) {
        for (EventTraceDataSourceConfiguration each : configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration()) {
            if (each.isActivated()) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public boolean add(final EventTraceDataSourceConfiguration config) {
        GlobalConfiguration configs = loadGlobal();
        boolean result = configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration().add(config);
        if (result) {
            configurationsXmlRepository.save(configs);
        }
        DataSource dataSource = DataSourceFactory.createDataSource(config);
        dynamicDataSource.addDataSource(config.getName(), dataSource);
        return result;
    }
    
    @Override
    public void delete(final String name) {
        GlobalConfiguration configs = loadGlobal();
        EventTraceDataSourceConfiguration toBeRemovedConfig = find(name, configs.getEventTraceDataSourceConfigurations());
        if (null != toBeRemovedConfig) {
            configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration().remove(toBeRemovedConfig);
            configurationsXmlRepository.save(configs);
        }
    }
    
    private GlobalConfiguration loadGlobal() {
        GlobalConfiguration result = configurationsXmlRepository.load();
        if (null == result.getEventTraceDataSourceConfigurations()) {
            result.setEventTraceDataSourceConfigurations(new EventTraceDataSourceConfigurations());
        }
        return result;
    }
}
