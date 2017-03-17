package com.dangdang.ddframe.job.lite.console.service.impl;

import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfigurations;
import com.dangdang.ddframe.job.lite.console.repository.DataSourceConfigurationsXmlRepository;
import com.dangdang.ddframe.job.lite.console.repository.impl.DataSourceConfigurationsXmlRepositoryImpl;
import com.dangdang.ddframe.job.lite.console.service.DataSourceService;
import com.google.common.base.Optional;

public class DataSourceServiceImpl implements DataSourceService {
    
    private DataSourceConfigurationsXmlRepository dataSourceConfigurationsXmlRepository = new DataSourceConfigurationsXmlRepositoryImpl();
    
    @Override
    public DataSourceConfigurations loadAll() {
        return dataSourceConfigurationsXmlRepository.load();
    }
    
    @Override
    public DataSourceConfiguration load(final String name) {
        DataSourceConfigurations configs = loadAll();
        DataSourceConfiguration result = findDataSourceConfiguration(name, configs);
        setActivated(configs, result);
        return result;
    }
    
    public DataSourceConfiguration findDataSourceConfiguration(final String name, final DataSourceConfigurations configs) {
        for (DataSourceConfiguration each : configs.getDataSourceConfiguration()) {
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
    
    private void setActivated(final DataSourceConfigurations configs, final DataSourceConfiguration toBeConnectedConfig) {
        DataSourceConfiguration activatedConfig = findActivatedDataSourceConfiguration(configs);
        if (!toBeConnectedConfig.equals(activatedConfig)) {
            if (null != activatedConfig) {
                activatedConfig.setActivated(false);
            }
            toBeConnectedConfig.setActivated(true);
            dataSourceConfigurationsXmlRepository.save(configs);
        }
    }
    
    @Override
    public Optional<DataSourceConfiguration> loadActivated() {
        DataSourceConfigurations configs = loadAll();
        DataSourceConfiguration result = findActivatedDataSourceConfiguration(configs);
        if (null == result) {
            return Optional.absent();
        }
        return Optional.of(result);
    }
    
    private DataSourceConfiguration findActivatedDataSourceConfiguration(final DataSourceConfigurations configs) {
        for (DataSourceConfiguration each : configs.getDataSourceConfiguration()) {
            if (each.isActivated()) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public boolean add(final DataSourceConfiguration config) {
        DataSourceConfigurations configs = loadAll();
        boolean result = configs.getDataSourceConfiguration().add(config);
        if (result) {
            dataSourceConfigurationsXmlRepository.save(configs);
        }
        return result;
    }
    
    @Override
    public void delete(final String name) {
        DataSourceConfigurations configs = loadAll();
        if (configs.getDataSourceConfiguration().remove(new DataSourceConfiguration(name, null, null, null))) {
            dataSourceConfigurationsXmlRepository.save(configs);
        }
    }
}
