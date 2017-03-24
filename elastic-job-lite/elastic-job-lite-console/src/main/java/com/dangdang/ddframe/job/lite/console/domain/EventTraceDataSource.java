package com.dangdang.ddframe.job.lite.console.domain;

import java.sql.DriverManager;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件追踪数据源.
 * 
 * @author zhangxinguo
 */
@Slf4j
public class EventTraceDataSource {
    
    @Getter(AccessLevel.PROTECTED)
    private EventTraceDataSourceConfiguration dataSourceConfig;
    
    public EventTraceDataSource(final EventTraceDataSourceConfiguration dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }
    
    public void init() {
        log.debug("Elastic job: data source init, connection url is: {}.", dataSourceConfig.getUrl());
        try {
            Class.forName(dataSourceConfig.getDriver());
            DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
