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

package org.apache.shardingsphere.elasticjob.tracing.rdb.storage;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.Source;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.tracing.rdb.type.DatabaseType;
import org.apache.shardingsphere.elasticjob.tracing.rdb.type.impl.DefaultDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

/**
 * RDB job event storage.
 */
@Slf4j
public final class RDBJobEventStorage {
    
    private static final String TABLE_JOB_EXECUTION_LOG = "JOB_EXECUTION_LOG";
    
    private static final String TABLE_JOB_STATUS_TRACE_LOG = "JOB_STATUS_TRACE_LOG";
    
    private static final String TASK_ID_STATE_INDEX = "TASK_ID_STATE_INDEX";
    
    private static final Map<String, DatabaseType> DATABASE_TYPES = new HashMap<>();
    
    private final DataSource dataSource;
    
    private final DatabaseType databaseType;
    
    private final RDBStorageSQLMapper sqlMapper;
    
    static {
        for (DatabaseType each : ServiceLoader.load(DatabaseType.class)) {
            DATABASE_TYPES.put(each.getType(), each);
        }
    }
    
    public RDBJobEventStorage(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        databaseType = getDatabaseType(dataSource);
        sqlMapper = new RDBStorageSQLMapper(databaseType.getSQLPropertiesFile());
        initTablesAndIndexes();
    }
    
    private DatabaseType getDatabaseType(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            for (DatabaseType each : DATABASE_TYPES.values()) {
                if (each.getDatabaseProductName().equals(databaseProductName)) {
                    return each;
                }
            }
        }
        return new DefaultDatabaseType();
    }
    
    private void initTablesAndIndexes() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            createJobExecutionTableAndIndexIfNeeded(connection);
            createJobStatusTraceTableAndIndexIfNeeded(connection);
        }
    }
    
    private void createJobExecutionTableAndIndexIfNeeded(final Connection connection) throws SQLException {
        if (existsTable(connection, TABLE_JOB_EXECUTION_LOG) || existsTable(connection, TABLE_JOB_EXECUTION_LOG.toLowerCase())) {
            return;
        }
        createJobExecutionTable(connection);
    }
    
    private void createJobStatusTraceTableAndIndexIfNeeded(final Connection connection) throws SQLException {
        if (existsTable(connection, TABLE_JOB_STATUS_TRACE_LOG) || existsTable(connection, TABLE_JOB_STATUS_TRACE_LOG.toLowerCase())) {
            return;
        }
        createJobStatusTraceTable(connection);
        createTaskIdIndexIfNeeded(connection);
    }
    
    private boolean existsTable(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData dbMetaData = connection.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }
    
    private void createTaskIdIndexIfNeeded(final Connection connection) throws SQLException {
        if (existsIndex(connection, TABLE_JOB_STATUS_TRACE_LOG, TASK_ID_STATE_INDEX) || existsIndex(connection, TABLE_JOB_STATUS_TRACE_LOG.toLowerCase(), TASK_ID_STATE_INDEX.toLowerCase())) {
            return;
        }
        createTaskIdAndStateIndex(connection);
    }
    
    private boolean existsIndex(final Connection connection, final String tableName, final String indexName) throws SQLException {
        DatabaseMetaData dbMetaData = connection.getMetaData();
        try (ResultSet resultSet = dbMetaData.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (resultSet.next()) {
                if (indexName.equals(resultSet.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void createJobExecutionTable(final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getCreateTableForJobExecutionLog())) {
            preparedStatement.execute();
        }
    }
    
    private void createJobStatusTraceTable(final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getCreateTableForJobStatusTraceLog())) {
            preparedStatement.execute();
        }
    }
    
    private void createTaskIdAndStateIndex(final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getCreateIndexForTaskIdStateIndex())) {
            preparedStatement.execute();
        }
    }
    
    /**
     * Add job execution event.
     * 
     * @param jobExecutionEvent job execution event
     * @return add success or not
     */
    public boolean addJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        if (null == jobExecutionEvent.getCompleteTime()) {
            return insertJobExecutionEvent(jobExecutionEvent);
        } else {
            if (jobExecutionEvent.isSuccess()) {
                return updateJobExecutionEventWhenSuccess(jobExecutionEvent);
            } else {
                return updateJobExecutionEventFailure(jobExecutionEvent);
            }
        }
    }
    
    private boolean insertJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLog())) {
            preparedStatement.setString(1, jobExecutionEvent.getId());
            preparedStatement.setString(2, jobExecutionEvent.getJobName());
            preparedStatement.setString(3, jobExecutionEvent.getTaskId());
            preparedStatement.setString(4, jobExecutionEvent.getHostname());
            preparedStatement.setString(5, jobExecutionEvent.getIp());
            preparedStatement.setInt(6, jobExecutionEvent.getShardingItem());
            preparedStatement.setString(7, jobExecutionEvent.getSource().toString());
            preparedStatement.setBoolean(8, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(9, new Timestamp(jobExecutionEvent.getStartTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            if (!isDuplicateRecord(ex)) {
                // TODO log failure directly to output log, consider to be configurable in the future
                log.error(ex.getMessage());
            }
        }
        return result;
    }
    
    private boolean updateJobExecutionEventWhenSuccess(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getUpdateForJobExecutionLog())) {
            preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
            preparedStatement.setString(3, jobExecutionEvent.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenSuccess(jobExecutionEvent);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenSuccess(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLogForComplete())) {
            preparedStatement.setString(1, jobExecutionEvent.getId());
            preparedStatement.setString(2, jobExecutionEvent.getJobName());
            preparedStatement.setString(3, jobExecutionEvent.getTaskId());
            preparedStatement.setString(4, jobExecutionEvent.getHostname());
            preparedStatement.setString(5, jobExecutionEvent.getIp());
            preparedStatement.setInt(6, jobExecutionEvent.getShardingItem());
            preparedStatement.setString(7, jobExecutionEvent.getSource().toString());
            preparedStatement.setBoolean(8, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(9, new Timestamp(jobExecutionEvent.getStartTime().getTime()));
            preparedStatement.setTimestamp(10, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            if (isDuplicateRecord(ex)) {
                return updateJobExecutionEventWhenSuccess(jobExecutionEvent);
            }
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean updateJobExecutionEventFailure(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getUpdateForJobExecutionLogForFailure())) {
            preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
            preparedStatement.setString(3, truncateString(jobExecutionEvent.getFailureCause()));
            preparedStatement.setString(4, jobExecutionEvent.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenFailure(jobExecutionEvent);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenFailure(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLogForFailure())) {
            preparedStatement.setString(1, jobExecutionEvent.getId());
            preparedStatement.setString(2, jobExecutionEvent.getJobName());
            preparedStatement.setString(3, jobExecutionEvent.getTaskId());
            preparedStatement.setString(4, jobExecutionEvent.getHostname());
            preparedStatement.setString(5, jobExecutionEvent.getIp());
            preparedStatement.setInt(6, jobExecutionEvent.getShardingItem());
            preparedStatement.setString(7, jobExecutionEvent.getSource().toString());
            preparedStatement.setString(8, truncateString(jobExecutionEvent.getFailureCause()));
            preparedStatement.setBoolean(9, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(10, new Timestamp(jobExecutionEvent.getStartTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            if (isDuplicateRecord(ex)) {
                return updateJobExecutionEventFailure(jobExecutionEvent);
            }
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean isDuplicateRecord(final SQLException ex) {
        return null != databaseType && databaseType.getDuplicateRecordErrorCode() == ex.getErrorCode();
    }
    
    /**
     * Add job status trace event.
     * 
     * @param jobStatusTraceEvent job status trace event
     * @return add success or not
     */
    public boolean addJobStatusTraceEvent(final JobStatusTraceEvent jobStatusTraceEvent) {
        String originalTaskId = jobStatusTraceEvent.getOriginalTaskId();
        if (State.TASK_STAGING != jobStatusTraceEvent.getState()) {
            originalTaskId = getOriginalTaskId(jobStatusTraceEvent.getTaskId());
        }
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobStatusTraceLog())) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, jobStatusTraceEvent.getJobName());
            preparedStatement.setString(3, originalTaskId);
            preparedStatement.setString(4, jobStatusTraceEvent.getTaskId());
            preparedStatement.setString(5, jobStatusTraceEvent.getSlaveId());
            preparedStatement.setString(6, jobStatusTraceEvent.getSource().toString());
            preparedStatement.setString(7, jobStatusTraceEvent.getExecutionType());
            preparedStatement.setString(8, jobStatusTraceEvent.getShardingItems());
            preparedStatement.setString(9, jobStatusTraceEvent.getState().toString());
            preparedStatement.setString(10, truncateString(jobStatusTraceEvent.getMessage()));
            preparedStatement.setTimestamp(11, new Timestamp(jobStatusTraceEvent.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private String getOriginalTaskId(final String taskId) {
        String result = "";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getSelectOriginalTaskIdForJobStatusTraceLog())) {
            preparedStatement.setString(1, taskId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("original_task_id");
                }
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private String truncateString(final String str) {
        return !Strings.isNullOrEmpty(str) && str.length() > 4000 ? str.substring(0, 4000) : str;
    }
    
    List<JobStatusTraceEvent> getJobStatusTraceEvents(final String taskId) {
        List<JobStatusTraceEvent> result = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getSelectForJobStatusTraceLog())) {
            preparedStatement.setString(1, taskId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
                            resultSet.getString(5), Source.valueOf(resultSet.getString(6)), resultSet.getString(7), resultSet.getString(8),
                            State.valueOf(resultSet.getString(9)), resultSet.getString(10), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(11)));
                    result.add(jobStatusTraceEvent);
                }
            }
        } catch (final SQLException | ParseException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
}
