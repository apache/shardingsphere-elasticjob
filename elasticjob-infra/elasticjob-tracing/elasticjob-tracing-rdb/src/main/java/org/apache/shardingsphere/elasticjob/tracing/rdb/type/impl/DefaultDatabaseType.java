package org.apache.shardingsphere.elasticjob.tracing.rdb.type.impl;

import org.apache.shardingsphere.elasticjob.tracing.rdb.type.DatabaseType;

/**
 * Default database type.
 */
public class DefaultDatabaseType implements DatabaseType {
    
    @Override
    public String getType() {
        return "SQL92";
    }
    
    @Override
    public int getDuplicateRecordErrorCode() {
        return Integer.MIN_VALUE;
    }
}
