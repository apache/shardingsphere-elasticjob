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
    private EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration;
    
    public EventTraceDataSource(final EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration) {
        this.eventTraceDataSourceConfiguration = eventTraceDataSourceConfiguration;
    }
    
    public void init() {
        log.debug("Elastic job: data source init, connection url is: {}.", eventTraceDataSourceConfiguration.getUrl());
        try {
            Class.forName(eventTraceDataSourceConfiguration.getDriver());
            DriverManager.getConnection(eventTraceDataSourceConfiguration.getUrl(), eventTraceDataSourceConfiguration.getUsername(), eventTraceDataSourceConfiguration.getPassword());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
