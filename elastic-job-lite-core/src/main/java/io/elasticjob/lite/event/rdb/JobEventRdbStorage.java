/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.event.rdb;

import com.google.common.base.Strings;
import io.elasticjob.lite.event.type.JobStatusTraceEvent.Source;
import io.elasticjob.lite.event.type.JobStatusTraceEvent.State;
import io.elasticjob.lite.context.ExecutionType;
import io.elasticjob.lite.event.type.JobExecutionEvent;
import io.elasticjob.lite.event.type.JobStatusTraceEvent;
import lombok.extern.slf4j.Slf4j;

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
import java.util.List;
import java.util.UUID;

/**
 * 运行痕迹事件数据库存储.
 *
 * @author caohao
 */
@Slf4j
final class JobEventRdbStorage {
    
    private static final String TABLE_JOB_EXECUTION_LOG = "JOB_EXECUTION_LOG";
    
    private static final String TABLE_JOB_STATUS_TRACE_LOG = "JOB_STATUS_TRACE_LOG";
    
    private static final String TASK_ID_STATE_INDEX = "TASK_ID_STATE_INDEX";
    
    private final DataSource dataSource;
    
    private DatabaseType databaseType;
    
    JobEventRdbStorage(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        initTablesAndIndexes();
    }
    
    private void initTablesAndIndexes() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            createJobExecutionTableAndIndexIfNeeded(conn);
            createJobStatusTraceTableAndIndexIfNeeded(conn);
            databaseType = DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName());
        }
    }
    
    private void createJobExecutionTableAndIndexIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_EXECUTION_LOG, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                createJobExecutionTable(conn);
            }
        }
    }
    
    private void createJobStatusTraceTableAndIndexIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_STATUS_TRACE_LOG, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                createJobStatusTraceTable(conn);
            }
        }
        createTaskIdIndexIfNeeded(conn, TABLE_JOB_STATUS_TRACE_LOG, TASK_ID_STATE_INDEX);
    }
    
    private void createTaskIdIndexIfNeeded(final Connection conn, final String tableName, final String indexName) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getIndexInfo(null, null, tableName, false, false)) {
            boolean hasTaskIdIndex = false;
            while (resultSet.next()) {
                if (indexName.equals(resultSet.getString("INDEX_NAME"))) {
                    hasTaskIdIndex = true;    
                }
            }
            if (!hasTaskIdIndex) {
                createTaskIdAndStateIndex(conn, tableName);
            }
        }
    }
    
    private void createJobExecutionTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_EXECUTION_LOG + "` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`task_id` VARCHAR(255) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`sharding_item` INT NOT NULL, "
                + "`execution_source` VARCHAR(20) NOT NULL, "
                + "`failure_cause` VARCHAR(4000) NULL, "
                + "`is_success` INT NOT NULL, "
                + "`start_time` TIMESTAMP NULL, "
                + "`complete_time` TIMESTAMP NULL, "
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobStatusTraceTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_STATUS_TRACE_LOG + "` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`original_task_id` VARCHAR(255) NOT NULL, "
                + "`task_id` VARCHAR(255) NOT NULL, "
                + "`slave_id` VARCHAR(50) NOT NULL, "
                + "`source` VARCHAR(50) NOT NULL, "
                + "`execution_type` VARCHAR(20) NOT NULL, "
                + "`sharding_item` VARCHAR(100) NOT NULL, "
                + "`state` VARCHAR(20) NOT NULL, "
                + "`message` VARCHAR(4000) NULL, "
                + "`creation_time` TIMESTAMP NULL, "
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createTaskIdAndStateIndex(final Connection conn, final String tableName) throws SQLException {
        String sql = "CREATE INDEX " + TASK_ID_STATE_INDEX + " ON " + tableName + " (`task_id`, `state`);";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.execute();
        }
    }
    
    boolean addJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
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
        String sql = "INSERT INTO `" + TABLE_JOB_EXECUTION_LOG + "` (`id`, `job_name`, `task_id`, `hostname`, `ip`, `sharding_item`, `execution_source`, `is_success`, `start_time`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
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
                // TODO 记录失败直接输出日志,未来可考虑配置化
                log.error(ex.getMessage());    
            }
        }
        return result;
    }
    
    private boolean isDuplicateRecord(final SQLException ex) {
        return DatabaseType.MySQL.equals(databaseType) && 1062 == ex.getErrorCode() || DatabaseType.H2.equals(databaseType) && 23505 == ex.getErrorCode() 
                || DatabaseType.SQLServer.equals(databaseType) && 1 == ex.getErrorCode() || DatabaseType.DB2.equals(databaseType) && -803 == ex.getErrorCode()
                || DatabaseType.PostgreSQL.equals(databaseType) && 0 == ex.getErrorCode() || DatabaseType.Oracle.equals(databaseType) && 1 == ex.getErrorCode();
    }
    
    private boolean updateJobExecutionEventWhenSuccess(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        String sql = "UPDATE `" + TABLE_JOB_EXECUTION_LOG + "` SET `is_success` = ?, `complete_time` = ? WHERE id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
            preparedStatement.setString(3, jobExecutionEvent.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenSuccess(jobExecutionEvent);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenSuccess(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_EXECUTION_LOG + "` (`id`, `job_name`, `task_id`, `hostname`, `ip`, `sharding_item`, `execution_source`, `is_success`, `start_time`, `complete_time`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
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
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean updateJobExecutionEventFailure(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        String sql = "UPDATE `" + TABLE_JOB_EXECUTION_LOG + "` SET `is_success` = ?, `complete_time` = ?, `failure_cause` = ? WHERE id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
            preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
            preparedStatement.setString(3, truncateString(jobExecutionEvent.getFailureCause()));
            preparedStatement.setString(4, jobExecutionEvent.getId());
            if (0 == preparedStatement.executeUpdate()) {
                return insertJobExecutionEventWhenFailure(jobExecutionEvent);
            }
            result = true;
        } catch (final SQLException ex) {
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private boolean insertJobExecutionEventWhenFailure(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_EXECUTION_LOG + "` (`id`, `job_name`, `task_id`, `hostname`, `ip`, `sharding_item`, `execution_source`, `failure_cause`, `is_success`, `start_time`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
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
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    boolean addJobStatusTraceEvent(final JobStatusTraceEvent jobStatusTraceEvent) {
        String originalTaskId = jobStatusTraceEvent.getOriginalTaskId();
        if (State.TASK_STAGING != jobStatusTraceEvent.getState()) {
            originalTaskId = getOriginalTaskId(jobStatusTraceEvent.getTaskId());
        }
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_STATUS_TRACE_LOG + "` (`id`, `job_name`, `original_task_id`, `task_id`, `slave_id`, `source`, `execution_type`, `sharding_item`,  " 
                + "`state`, `message`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, jobStatusTraceEvent.getJobName());
            preparedStatement.setString(3, originalTaskId);
            preparedStatement.setString(4, jobStatusTraceEvent.getTaskId());
            preparedStatement.setString(5, jobStatusTraceEvent.getSlaveId());
            preparedStatement.setString(6, jobStatusTraceEvent.getSource().toString());
            preparedStatement.setString(7, jobStatusTraceEvent.getExecutionType().name());
            preparedStatement.setString(8, jobStatusTraceEvent.getShardingItems());
            preparedStatement.setString(9, jobStatusTraceEvent.getState().toString());
            preparedStatement.setString(10, truncateString(jobStatusTraceEvent.getMessage()));
            preparedStatement.setTimestamp(11, new Timestamp(jobStatusTraceEvent.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private String getOriginalTaskId(final String taskId) {
        String sql = String.format("SELECT original_task_id FROM %s WHERE task_id = '%s' and state='%s' LIMIT 1", TABLE_JOB_STATUS_TRACE_LOG, taskId, State.TASK_STAGING);
        String result = "";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getString("original_task_id");
            }
        } catch (final SQLException ex) {
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
    
    private String truncateString(final String str) {
        return !Strings.isNullOrEmpty(str) && str.length() > 4000 ? str.substring(0, 4000) : str;
    }
    
    List<JobStatusTraceEvent> getJobStatusTraceEvents(final String taskId) {
        String sql = String.format("SELECT * FROM %s WHERE task_id = '%s'", TABLE_JOB_STATUS_TRACE_LOG, taskId);
        List<JobStatusTraceEvent> result = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
                        resultSet.getString(5), Source.valueOf(resultSet.getString(6)), ExecutionType.valueOf(resultSet.getString(7)), resultSet.getString(8),
                        State.valueOf(resultSet.getString(9)), resultSet.getString(10), new SimpleDateFormat("yyyy-mm-dd HH:MM:SS").parse(resultSet.getString(11)));
                result.add(jobStatusTraceEvent);
            }
        } catch (final SQLException | ParseException ex) {
            // TODO 记录失败直接输出日志,未来可考虑配置化
            log.error(ex.getMessage());
        }
        return result;
    }
}
