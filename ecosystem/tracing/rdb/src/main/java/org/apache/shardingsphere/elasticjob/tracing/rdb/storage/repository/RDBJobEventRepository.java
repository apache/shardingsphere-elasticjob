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

package org.apache.shardingsphere.elasticjob.tracing.rdb.storage.repository;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.kernel.tracing.exception.WrapException;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.sql.RDBStorageSQLMapper;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.sql.SQLPropertiesFactory;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.type.TracingStorageDatabaseType;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.type.impl.DefaultTracingStorageDatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * RDB job event repository.
 */
@Slf4j
public final class RDBJobEventRepository {
    
    private static final String TABLE_JOB_EXECUTION_LOG = "JOB_EXECUTION_LOG";
    
    private static final String TABLE_JOB_STATUS_TRACE_LOG = "JOB_STATUS_TRACE_LOG";
    
    private static final String TASK_ID_STATE_INDEX = "TASK_ID_STATE_INDEX";
    
    private static final Map<DataSource, RDBJobEventRepository> STORAGE_MAP = new ConcurrentHashMap<>();
    
    private final DataSource dataSource;
    
    private final TracingStorageDatabaseType tracingStorageDatabaseType;
    
    private final RDBStorageSQLMapper sqlMapper;
    
    private RDBJobEventRepository(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        tracingStorageDatabaseType = getTracingStorageDatabaseType(dataSource);
        sqlMapper = new RDBStorageSQLMapper(SQLPropertiesFactory.getProperties(tracingStorageDatabaseType));
        initTablesAndIndexes();
    }
    
    /**
     * The same dataSource always return the same RDBJobEventStorage instance.
     *
     * @param dataSource dataSource
     * @return RDBJobEventStorage instance
     * @throws SQLException SQLException
     */
    public static RDBJobEventRepository getInstance(final DataSource dataSource) throws SQLException {
        return wrapException(() -> STORAGE_MAP.computeIfAbsent(dataSource, ds -> {
            try {
                return new RDBJobEventRepository(ds);
            } catch (final SQLException ex) {
                throw new WrapException(ex);
            }
        }));
    }
    
    private static RDBJobEventRepository wrapException(final Supplier<RDBJobEventRepository> supplier) throws SQLException {
        try {
            return supplier.get();
        } catch (final WrapException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw new SQLException(ex.getCause());
            }
            throw ex;
        }
    }
    
    private TracingStorageDatabaseType getTracingStorageDatabaseType(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            for (TracingStorageDatabaseType each : ShardingSphereServiceLoader.getServiceInstances(TracingStorageDatabaseType.class)) {
                if (each.getDatabaseProductName().equals(databaseProductName)) {
                    return each;
                }
            }
        }
        return new DefaultTracingStorageDatabaseType();
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
     * @param event job execution event
     * @return add success or not
     */
    public boolean addJobExecutionEvent(final JobExecutionEvent event) {
        if (null == event.getCompleteTime()) {
            return insertJobExecutionEvent(event);
        } else {
            if (event.isSuccess()) {
                return updateJobExecutionEventWhenSuccess(event);
            } else {
                return updateJobExecutionEventFailure(event);
            }
        }
    }
    
    private boolean insertJobExecutionEvent(final JobExecutionEvent event) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLog())) {
            preparedStatement.setString(1, event.getId());
            preparedStatement.setString(2, event.getJobName());
            preparedStatement.setString(3, event.getTaskId());
            preparedStatement.setString(4, event.getHostname());
            preparedStatement.setString(5, event.getIp());
            preparedStatement.setInt(6, event.getShardingItem());
            preparedStatement.setString(7, event.getSource().toString());
            preparedStatement.setBoolean(8, event.isSuccess());
            preparedStatement.setTimestamp(9, new Timestamp(event.getStartTime().getTime()));
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
    
    private boolean updateJobExecutionEventWhenSuccess(final JobExecutionEvent event) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getUpdateForJobExecutionLog())) {
            preparedStatement.setBoolean(1, event.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(event.getCompleteTime().getTime()));
            preparedStatement.setString(3, event.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenSuccess(event);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenSuccess(final JobExecutionEvent event) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLogForComplete())) {
            preparedStatement.setString(1, event.getId());
            preparedStatement.setString(2, event.getJobName());
            preparedStatement.setString(3, event.getTaskId());
            preparedStatement.setString(4, event.getHostname());
            preparedStatement.setString(5, event.getIp());
            preparedStatement.setInt(6, event.getShardingItem());
            preparedStatement.setString(7, event.getSource().toString());
            preparedStatement.setBoolean(8, event.isSuccess());
            preparedStatement.setTimestamp(9, new Timestamp(event.getStartTime().getTime()));
            preparedStatement.setTimestamp(10, new Timestamp(event.getCompleteTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            if (isDuplicateRecord(ex)) {
                return updateJobExecutionEventWhenSuccess(event);
            }
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean updateJobExecutionEventFailure(final JobExecutionEvent event) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getUpdateForJobExecutionLogForFailure())) {
            preparedStatement.setBoolean(1, event.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(event.getCompleteTime().getTime()));
            preparedStatement.setString(3, truncateString(event.getFailureCause()));
            preparedStatement.setString(4, event.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenFailure(event);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenFailure(final JobExecutionEvent event) {
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobExecutionLogForFailure())) {
            preparedStatement.setString(1, event.getId());
            preparedStatement.setString(2, event.getJobName());
            preparedStatement.setString(3, event.getTaskId());
            preparedStatement.setString(4, event.getHostname());
            preparedStatement.setString(5, event.getIp());
            preparedStatement.setInt(6, event.getShardingItem());
            preparedStatement.setString(7, event.getSource().toString());
            preparedStatement.setString(8, truncateString(event.getFailureCause()));
            preparedStatement.setBoolean(9, event.isSuccess());
            preparedStatement.setTimestamp(10, new Timestamp(event.getStartTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            if (isDuplicateRecord(ex)) {
                return updateJobExecutionEventFailure(event);
            }
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean isDuplicateRecord(final SQLException ex) {
        return null != tracingStorageDatabaseType && tracingStorageDatabaseType.getDuplicateRecordErrorCode() == ex.getErrorCode();
    }
    
    /**
     * Add job status trace event.
     * 
     * @param event job status trace event
     * @return add success or not
     */
    public boolean addJobStatusTraceEvent(final JobStatusTraceEvent event) {
        String originalTaskId = event.getOriginalTaskId();
        if (State.TASK_STAGING != event.getState()) {
            originalTaskId = getOriginalTaskId(event.getTaskId());
        }
        boolean result = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlMapper.getInsertForJobStatusTraceLog())) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, event.getJobName());
            preparedStatement.setString(3, originalTaskId);
            preparedStatement.setString(4, event.getTaskId());
            preparedStatement.setString(5, event.getSlaveId());
            preparedStatement.setString(6, event.getExecutionType().name());
            preparedStatement.setString(7, event.getShardingItems());
            preparedStatement.setString(8, event.getState().toString());
            preparedStatement.setString(9, truncateString(event.getMessage()));
            preparedStatement.setTimestamp(10, new Timestamp(event.getCreationTime().getTime()));
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
}
