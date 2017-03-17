package com.dangdang.ddframe.job.lite.console.service;

import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfigurations;
import com.google.common.base.Optional;

public interface DataSourceService {
    
    DataSourceConfigurations loadAll();
    
    DataSourceConfiguration load(String name);
    
    Optional<DataSourceConfiguration> loadActivated();
    
    boolean add(DataSourceConfiguration config);
    
    void delete(String name);
}
