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

import javax.sql.DataSource;
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
class JobEventRdbStorage {
    
    private final DataSource dataSource;
    
    private final LogLevel logLevel;
    
    JobEventRdbStorage(final String driverClassName, final String url, final String username, final String password, final LogLevel logLevel) throws SQLException {
        this.logLevel = logLevel;
        dataSource = JobEventRdbDataSourceFactory.getDataSource(driverClassName, url, username, password);
        createJobExecutionTable();
        createJobTraceTable();
    }
    
    boolean addJobTraceEvent(final JobTraceEvent traceEvent) {
        boolean result = false;
        if (needTrace(traceEvent.getLogLevel())) {
            String sql = "INSERT INTO `JOB_TRACE_LOG` (`id`, `job_name`, `hostname`, `ip`, `log_level`, `message`, `failure_cause`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, UUID.randomUUID().toString());
                preparedStatement.setString(2, traceEvent.getJobName());
                preparedStatement.setString(3, traceEvent.getHostname());
                preparedStatement.setString(4, traceEvent.getIp());
                preparedStatement.setString(5, traceEvent.getLogLevel().name());
                preparedStatement.setString(6, traceEvent.getMessage());
                preparedStatement.setString(7, truncateFailureCause(traceEvent.getFailureCause()));
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
            String sql = "INSERT INTO `JOB_EXECUTION_LOG` (`id`, `job_name`, `hostname`, `ip`, `sharding_item`, `execution_source`, `is_success`, `start_time`) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, jobExecutionEvent.getId());
                preparedStatement.setString(2, jobExecutionEvent.getJobName());
                preparedStatement.setString(3, jobExecutionEvent.getHostname());
                preparedStatement.setString(4, jobExecutionEvent.getIp());
                preparedStatement.setInt(5, jobExecutionEvent.getShardingItem());
                preparedStatement.setString(6, jobExecutionEvent.getSource().toString());
                preparedStatement.setBoolean(7, jobExecutionEvent.isSuccess());
                preparedStatement.setTimestamp(8, new Timestamp(jobExecutionEvent.getStartTime().getTime()));
                preparedStatement.execute();
                result = true;
            } catch (final SQLException ex) {
                // TODO 记录失败直接输出日志,未来可考虑配置化
                log.error(ex.getMessage());
            }
        } else {
            if (jobExecutionEvent.isSuccess()) {
                String sql = "UPDATE `JOB_EXECUTION_LOG` SET `is_success` = ?, `complete_time` = ? WHERE id = ?";
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
                String sql = "UPDATE `JOB_EXECUTION_LOG` SET `is_success` = ?, `failure_cause` = ? WHERE id = ?";
                try (
                        Connection conn = dataSource.getConnection();
                        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setBoolean(1, jobExecutionEvent.isSuccess());
                    preparedStatement.setString(2, truncateFailureCause(jobExecutionEvent.getFailureCause()));
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
    
    private boolean needTrace(final LogLevel logLevel) {
        return logLevel.ordinal() >= this.logLevel.ordinal();
    }
    
    private String truncateFailureCause(final String failureCause) {
        return !Strings.isNullOrEmpty(failureCause) && failureCause.length() > 4000 ? failureCause.substring(0, 4000) : failureCause;
    }
    
    private void createJobTraceTable() throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `JOB_TRACE_LOG` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`log_level` CHAR(5) NOT NULL, "
                + "`message` VARCHAR(2000) NOT NULL, "
                + "`failure_cause` VARCHAR(4000) NULL, "
                + "`creation_time` TIMESTAMP NOT NULL, "
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobExecutionTable() throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `JOB_EXECUTION_LOG` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`job_name` VARCHAR(100) NOT NULL, "
                + "`hostname` VARCHAR(255) NOT NULL, "
                + "`ip` VARCHAR(50) NOT NULL, "
                + "`sharding_item` INT NOT NULL, "
                + "`execution_source` VARCHAR(20) NOT NULL, "
                + "`failure_cause` VARCHAR(4000) NULL, "
                + "`is_success` BIT NOT NULL, "
                + "`start_time` TIMESTAMP NOT NULL, "
                + "`complete_time` TIMESTAMP NULL, "
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
}
