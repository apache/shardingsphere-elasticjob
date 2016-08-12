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

import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * 运行痕迹事件数据库存储.
 *
 * @author caohao
 */
@Slf4j
final class JobRdbEventStorage { 
    
    private final BasicDataSource dataSource;
    
    private final LogLevel logLevel;
    
    JobRdbEventStorage(final String driverClassName, final String url, final String username, final String password, final LogLevel logLevel) {
        this.logLevel = logLevel;
        // TODO 细化pool配置
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        try {
            createJobExecutionTable();
            createJobTraceTable();
        } catch (final SQLException ex) {
            // TODO common和api包合并后梳理异常 
            throw new RuntimeException(ex.getCause());
        }
    }
    
    boolean addJobTraceEvent(final JobTraceEvent traceEvent) {
        boolean result = false;
        if (needTrace(traceEvent.getLogLevel())) {
            String sql = "INSERT INTO `job_trace_log` (`id`, `job_name`, `hostname`, `message`, `failure_cause`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?);";
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, UUID.randomUUID().toString());
                preparedStatement.setString(2, traceEvent.getJobName());
                preparedStatement.setString(3, traceEvent.getHostname());
                preparedStatement.setString(4, traceEvent.getMessage());
                preparedStatement.setString(5, getFailureCause(traceEvent.getFailureCause()));
                preparedStatement.setTimestamp(6, new Timestamp(traceEvent.getCreationTime().getTime()));
                preparedStatement.execute();
                result = true;
            } catch (final SQLException ex) {
                // TODO common和api包合并后梳理异常
                throw new RuntimeException(ex.getCause());
            }
        }
        return result;
    }
    
    boolean addJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        boolean result = false;
        if (null == jobExecutionEvent.getCompleteTime()) {
            String sql = "INSERT INTO `job_execution_log` (`id`, `job_name`, `hostname`, `sharding_items`, `execution_source`, `is_success`, `start_time`) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?);";
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, jobExecutionEvent.getId());
                preparedStatement.setString(2, jobExecutionEvent.getJobName());
                preparedStatement.setString(3, jobExecutionEvent.getHostname());
                preparedStatement.setString(4, jobExecutionEvent.getShardingItems().toString());
                preparedStatement.setString(5, jobExecutionEvent.getSource().toString());
                preparedStatement.setBoolean(6, jobExecutionEvent.isSuccess());
                preparedStatement.setTimestamp(7, new Timestamp(jobExecutionEvent.getStartTime().getTime()));
                preparedStatement.execute();
                result = true;
            } catch (final SQLException ex) {
                // TODO common和api包合并后梳理异常
                throw new RuntimeException(ex.getCause());
            }
        } else {
            if (jobExecutionEvent.isSuccess()) {
                String sql = "UPDATE `job_execution_log` SET `is_success` = ?, `complete_time` = ? WHERE id = ?";
                try (
                        Connection conn = dataSource.getConnection();
                        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
                    preparedStatement.setTimestamp(2, new Timestamp(jobExecutionEvent.getCompleteTime().getTime()));
                    preparedStatement.setString(3, jobExecutionEvent.getId());
                    preparedStatement.execute();
                    result = true;
                } catch (final SQLException ex) {
                    // TODO common和api包合并后梳理异常
                    throw new RuntimeException(ex.getCause());
                }
            } else {
                String sql = "UPDATE `job_execution_log` SET `is_success` = ?, `failure_cause` = ? WHERE id = ?";
                try (
                        Connection conn = dataSource.getConnection();
                        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
                    preparedStatement.setString(2, getFailureCause(jobExecutionEvent.getFailureCause()));
                    preparedStatement.setString(3, jobExecutionEvent.getId());
                    preparedStatement.execute();
                    result = true;
                } catch (final SQLException ex) {
                    // TODO common和api包合并后梳理异常
                    throw new RuntimeException(ex.getCause());
                }
            }
        }
        return result;
    }
    
    private boolean needTrace(final LogLevel logLevel) {
        return logLevel.ordinal() >= this.logLevel.ordinal();
    }
    
    private String getFailureCause(final String failureCause) {
        return !Strings.isNullOrEmpty(failureCause) && failureCause.length() > 65535 ? failureCause.substring(0, 65534) : ""; 
    }
    
    private void createJobTraceTable() throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `job_trace_log` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`hostname` VARCHAR(100) NOT NULL, "
                + "`message` VARCHAR(2000) NOT NULL, "
                + "`failure_cause` TEXT NULL, "
                + "`creation_time` TIMESTAMP NOT NULL, "
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobExecutionTable() throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `job_execution_log` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`hostname` VARCHAR(50) NOT NULL, "
                + "`sharding_items` VARCHAR(500) NOT NULL, "
                + "`execution_source` VARCHAR(20) NOT NULL, "
                + "`failure_cause` TEXT NULL, "
                + "`is_success` BIT NOT NULL, "
                + "`start_time` TIMESTAMP NOT NULL, "
                + "`complete_time` TIMESTAMP, "
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
}
