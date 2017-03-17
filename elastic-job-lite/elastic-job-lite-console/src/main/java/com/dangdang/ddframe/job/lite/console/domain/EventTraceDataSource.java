package com.dangdang.ddframe.job.lite.console.domain;

import java.sql.DriverManager;

import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;

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
    private DataSourceConfiguration dataSourceConfig;
    
    public EventTraceDataSource(final DataSourceConfiguration dataSourceConfig) {
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
