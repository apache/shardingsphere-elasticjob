/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.tracing.rdb.storage;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Properties;

/**
 * RDB storage SQL mapper.
 */
@Getter
public final class RDBStorageSQLMapper {
    
    private final String createTableForJobExecutionLog;
    
    private final String createTableForJobStatusTraceLog;
    
    private final String createIndexForTaskIdStateIndex;
    
    private final String insertForJobExecutionLog;
    
    private final String insertForJobExecutionLogForComplete;
    
    private final String insertForJobExecutionLogForFailure;
    
    private final String updateForJobExecutionLog;
    
    private final String updateForJobExecutionLogForFailure;
    
    private final String insertForJobStatusTraceLog;
    
    private final String selectForJobStatusTraceLog;
    
    private final String selectOriginalTaskIdForJobStatusTraceLog;
    
    public RDBStorageSQLMapper(final String databaseType) {
        Properties props = loadProps(databaseType);
        createTableForJobExecutionLog = props.getProperty("JOB_EXECUTION_LOG.TABLE.CREATE");
        createTableForJobStatusTraceLog = props.getProperty("JOB_STATUS_TRACE_LOG.TABLE.CREATE");
        createIndexForTaskIdStateIndex = props.getProperty("TASK_ID_STATE_INDEX.INDEX.CREATE");
        insertForJobExecutionLog = props.getProperty("JOB_EXECUTION_LOG.INSERT");
        insertForJobExecutionLogForComplete = props.getProperty("JOB_EXECUTION_LOG.INSERT_COMPLETE");
        insertForJobExecutionLogForFailure = props.getProperty("JOB_EXECUTION_LOG.INSERT_FAILURE");
        updateForJobExecutionLog = props.getProperty("JOB_EXECUTION_LOG.UPDATE");
        updateForJobExecutionLogForFailure = props.getProperty("JOB_EXECUTION_LOG.UPDATE_FAILURE");
        insertForJobStatusTraceLog = props.getProperty("JOB_STATUS_TRACE_LOG.INSERT");
        selectForJobStatusTraceLog = props.getProperty("JOB_STATUS_TRACE_LOG.SELECT");
        selectOriginalTaskIdForJobStatusTraceLog = props.getProperty("JOB_STATUS_TRACE_LOG.SELECT_ORIGINAL_TASK_ID");
    }
    
    @SneakyThrows
    private Properties loadProps(final String databaseType) {
        Properties result = new Properties();
        result.load(RDBJobEventStorage.class.getClassLoader().getResourceAsStream("META-INF/sql/storage/mysql.properties"));
        return result;
    }
}
