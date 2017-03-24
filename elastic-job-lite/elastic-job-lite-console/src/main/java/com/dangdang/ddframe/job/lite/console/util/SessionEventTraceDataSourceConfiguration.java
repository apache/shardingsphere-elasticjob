package com.dangdang.ddframe.job.lite.console.util;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionEventTraceDataSourceConfiguration {
    
    private static EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration;
    
    public static EventTraceDataSourceConfiguration getEventTraceDataSourceConfiguration() {
        return eventTraceDataSourceConfiguration;
    }
    
    public static void setDataSourceConfiguration(final EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration) {
        SessionEventTraceDataSourceConfiguration.eventTraceDataSourceConfiguration = eventTraceDataSourceConfiguration;
    }
}
