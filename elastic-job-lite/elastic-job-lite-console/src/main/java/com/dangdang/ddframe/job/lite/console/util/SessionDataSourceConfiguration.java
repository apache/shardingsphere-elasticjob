package com.dangdang.ddframe.job.lite.console.util;

import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionDataSourceConfiguration {
    
    private static DataSourceConfiguration dataSourceConfiguration;
    
    public static DataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }
    
    public static void setDataSourceConfiguration(final DataSourceConfiguration dataSourceConfiguration) {
        SessionDataSourceConfiguration.dataSourceConfiguration = dataSourceConfiguration;
    }
}
