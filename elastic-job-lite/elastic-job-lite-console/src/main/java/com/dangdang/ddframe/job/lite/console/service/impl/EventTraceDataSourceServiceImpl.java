package com.dangdang.ddframe.job.lite.console.service.impl;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfigurations;
import com.dangdang.ddframe.job.lite.console.domain.GlobalConfiguration;
import com.dangdang.ddframe.job.lite.console.repository.ConfigurationsXmlRepository;
import com.dangdang.ddframe.job.lite.console.repository.impl.ConfigurationsXmlRepositoryImpl;
import com.dangdang.ddframe.job.lite.console.service.EventTraceDataSourceService;
import com.google.common.base.Optional;

public class EventTraceDataSourceServiceImpl implements EventTraceDataSourceService {
    
    private ConfigurationsXmlRepository configurationsXmlRepository = new ConfigurationsXmlRepositoryImpl("EventTraceDataSourceConfigurations.xml");
    
    @Override
    public GlobalConfiguration loadAll() {
        GlobalConfiguration globalConfiguration = configurationsXmlRepository.load();
        if (null == globalConfiguration.getEventTraceDataSourceConfigurations()) {
            globalConfiguration.setEventTraceDataSourceConfigurations(new EventTraceDataSourceConfigurations());
        }
        return globalConfiguration;
    }
    
    @Override
    public EventTraceDataSourceConfiguration load(final String name) {
        GlobalConfiguration configs = loadAll();
        EventTraceDataSourceConfiguration result = findDataSourceConfiguration(name, configs);
        setActivated(configs, result);
        return result;
    }
    
    public EventTraceDataSourceConfiguration findDataSourceConfiguration(final String name, final GlobalConfiguration configs) {
        for (EventTraceDataSourceConfiguration each : configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration()) {
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
        GlobalConfiguration configs = loadAll();
        EventTraceDataSourceConfiguration result = findActivatedDataSourceConfiguration(configs);
        if (null == result) {
            return Optional.absent();
        }
        return Optional.of(result);
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
        GlobalConfiguration configs = loadAll();
        boolean result = configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration().add(config);
        if (result) {
            configurationsXmlRepository.save(configs);
        }
        return result;
    }
    
    @Override
    public void delete(final String name) {
        GlobalConfiguration configs = loadAll();
        if (configs.getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration().remove(new EventTraceDataSourceConfiguration(name, null, null, null))) {
            configurationsXmlRepository.save(configs);
        }
    }
}
