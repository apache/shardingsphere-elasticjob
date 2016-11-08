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

package com.dangdang.ddframe.job.event.rdb;

import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * 运行痕迹事件数据库存储.
 *
 * @author caohao
 */
@Slf4j
class JobEventRdbStorage {
    
    private static final String TABLE_JOB_TRACE_LOG = "JOB_TRACE_LOG";
    
    private static final String TABLE_JOB_EXECUTION_LOG = "JOB_EXECUTION_LOG";
    
    private static final String TABLE_JOB_STATUS_TRACE_LOG = "JOB_STATUS_TRACE_LOG";
    
    private final DataSource dataSource;
    
    private final LogLevel logLevel;
    
    JobEventRdbStorage(final String driverClassName, final String url, final String username, final String password, final LogLevel logLevel) throws SQLException {
        this.logLevel = logLevel;
        dataSource = JobEventRdbDataSourceFactory.getDataSource(driverClassName, url, username, password);
        initTables();
    }
    
    private void initTables() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_TRACE_LOG, new String[]{"TABLE"})) {
                if (!resultSet.next()) {
                    createJobTraceTable(conn);
                } 
            }
            try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_EXECUTION_LOG, new String[]{"TABLE"})) {
                if (!resultSet.next()) {
                    createJobExecutionTable(conn);
                }
            }
            try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_STATUS_TRACE_LOG, new String[]{"TABLE"})) {
                if (!resultSet.next()) {
                    createJobStatusTraceTable(conn);
                }
            }
        }
    }
    
    private void createJobTraceTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_TRACE_LOG + "` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`log_level` CHAR(5) NOT NULL, "
                + "`message` VARCHAR(2000) NOT NULL, "
                + "`failure_cause` VARCHAR(4000) NULL, "
                + "`creation_time` TIMESTAMP NOT NULL, "
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobExecutionTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_EXECUTION_LOG + "` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`task_id` VARCHAR(1000) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`sharding_item` INT NOT NULL, "
                + "`execution_source` VARCHAR(20) NOT NULL, "
                + "`failure_cause` VARCHAR(4000) NULL, "
                + "`is_success` BIT NOT NULL, "
                + "`start_time` TIMESTAMP NOT NULL, "
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
                + "`task_id` VARCHAR(1000) NOT NULL, "
                + "`slave_id` VARCHAR(1000) NOT NULL, "
                + "`execution_type` VARCHAR(20) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`sharding_item` INT NOT NULL, "
                + "`state` VARCHAR(20) NOT NULL, "
                + "`message` VARCHAR(4000) NULL, "
                + "`creation_time` TIMESTAMP NULL, "
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    boolean addJobTraceEvent(final JobTraceEvent traceEvent) {
        boolean result = false;
        if (needTrace(traceEvent.getLogLevel())) {
            String sql = "INSERT INTO `" + TABLE_JOB_TRACE_LOG + "` (`id`, `job_name`, `hostname`, `ip`, `log_level`, `message`, `failure_cause`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, UUID.randomUUID().toString());
                preparedStatement.setString(2, traceEvent.getJobName());
                preparedStatement.setString(3, traceEvent.getHostname());
                preparedStatement.setString(4, traceEvent.getIp());
                preparedStatement.setString(5, traceEvent.getLogLevel().name());
                preparedStatement.setString(6, traceEvent.getMessage());
                preparedStatement.setString(7, truncateString(traceEvent.getFailureCause()));
                preparedStatement.setTimestamp(8, new Timestamp(traceEvent.getCreationTime().getTime()));
                preparedStatement.execute();
                result = true;
            } catch (final SQLException ex) {
                // TODO 记录失败直接输出日志,未来可考虑配置化
                log.error(ex.getMessage());
            }
        }
        return result;
    }
    
    boolean addJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        if (null == jobExecutionEvent.getCompleteTime()) {
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
                // TODO 记录失败直接输出日志,未来可考虑配置化
                log.error(ex.getMessage());
            }
        } else {
            if (jobExecutionEvent.isSuccess()) {
                String sql = "UPDATE `" + TABLE_JOB_EXECUTION_LOG + "` SET `is_success` = ?, `complete_time` = ? WHERE id = ?";
                try (
                        Connection conn = dataSource.getConnection();
                        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
                    preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
                    preparedStatement.setString(3, jobExecutionEvent.getId());
                    preparedStatement.execute();
                    result = true;
                } catch (final SQLException ex) {
                    // TODO 记录失败直接输出日志,未来可考虑配置化
                    log.error(ex.getMessage());
                }
            } else {
                String sql = "UPDATE `" + TABLE_JOB_EXECUTION_LOG + "` SET `is_success` = ?, `failure_cause` = ? WHERE id = ?";
                try (
                        Connection conn = dataSource.getConnection();
                        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
                    preparedStatement.setString(2, truncateString(jobExecutionEvent.getFailureCause()));
                    preparedStatement.setString(3, jobExecutionEvent.getId());
                    preparedStatement.execute();
                    result = true;
                } catch (final SQLException ex) {
                    // TODO 记录失败直接输出日志,未来可考虑配置化
                    log.error(ex.getMessage());
                }
            }
        }
        return result;
    }
    
    boolean addJobStatusTraceEvent(final JobStatusTraceEvent jobStatusTraceEvent) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_STATUS_TRACE_LOG + "` (`id`, `job_name`, `task_id`, `slave_id`, `execution_type`, `hostname`, `ip`,  `sharding_item`,  " 
                + "`state`, `message`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, jobStatusTraceEvent.getJobName());
            preparedStatement.setString(3, jobStatusTraceEvent.getTaskId());
            preparedStatement.setString(4, jobStatusTraceEvent.getSlaveId());
            preparedStatement.setString(5, jobStatusTraceEvent.getExecutionType());
            preparedStatement.setString(6, jobStatusTraceEvent.getHostname());
            preparedStatement.setString(7, jobStatusTraceEvent.getIp());
            preparedStatement.setString(8, jobStatusTraceEvent.getShardingItem());
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
    
    private boolean needTrace(final LogLevel logLevel) {
        return logLevel.ordinal() >= this.logLevel.ordinal();
    }
    
    private String truncateString(final String str) {
        return !Strings.isNullOrEmpty(str) && str.length() > 4000 ? str.substring(0, 4000) : str;
    }
}
