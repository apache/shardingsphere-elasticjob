package com.dangdang.ddframe.job.lite.console.service;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.GlobalConfiguration;
import com.google.common.base.Optional;

public interface EventTraceDataSourceService {
    
    GlobalConfiguration loadAll();
    
    EventTraceDataSourceConfiguration load(String name);
    
    Optional<EventTraceDataSourceConfiguration> loadActivated();
    
    boolean add(EventTraceDataSourceConfiguration config);
    
    void delete(String name);
}
