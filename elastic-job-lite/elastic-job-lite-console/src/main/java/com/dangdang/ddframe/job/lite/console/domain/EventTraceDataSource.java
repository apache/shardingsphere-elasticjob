package com.dangdang.ddframe.job.lite.console.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 事件追踪数据源.
 * 
 * @author zhangxinguo
 */
@Slf4j
public final class EventTraceDataSource {
    
    @Getter(AccessLevel.PROTECTED)
    private EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration;
    
    public EventTraceDataSource(final EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration) {
        this.eventTraceDataSourceConfiguration = eventTraceDataSourceConfiguration;
    }
    
    public void init() {
        log.debug("Elastic job: data source init, connection url is: {}.", eventTraceDataSourceConfiguration.getUrl());
        try {
            Class.forName(eventTraceDataSourceConfiguration.getDriver());
            DriverManager.getConnection(eventTraceDataSourceConfiguration.getUrl(), eventTraceDataSourceConfiguration.getUsername(), eventTraceDataSourceConfiguration.getPassword());
        } catch (final ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
