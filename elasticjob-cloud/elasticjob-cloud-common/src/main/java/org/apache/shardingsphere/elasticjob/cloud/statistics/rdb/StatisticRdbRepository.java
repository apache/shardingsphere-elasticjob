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

package org.apache.shardingsphere.elasticjob.cloud.statistics.rdb;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Statistic RDB repository.
 */
@Slf4j
public class StatisticRdbRepository {
    
    private static final String TABLE_TASK_RESULT_STATISTICS = "TASK_RESULT_STATISTICS";
    
    private static final String TABLE_TASK_RUNNING_STATISTICS = "TASK_RUNNING_STATISTICS";

    private static final String TABLE_JOB_RUNNING_STATISTICS = "JOB_RUNNING_STATISTICS";
    
    private static final String TABLE_JOB_REGISTER_STATISTICS = "JOB_REGISTER_STATISTICS";
    
    private final DataSource dataSource;

    public StatisticRdbRepository(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        initTables();
    }
    
    private void initTables() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            createTaskResultTableIfNeeded(conn);
            createTaskRunningTableIfNeeded(conn);
            createJobRunningTableIfNeeded(conn);
            createJobRegisterTableIfNeeded(conn);
        }
    }
    
    private void createTaskResultTableIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        for (StatisticInterval each : StatisticInterval.values()) {
            try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_TASK_RESULT_STATISTICS + "_" + each, new String[]{"TABLE"})) {
                if (!resultSet.next()) {
                    createTaskResultTable(conn, each);
                }
            }
        }
    }
    
    private void createTaskResultTable(final Connection conn, final StatisticInterval statisticInterval) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_TASK_RESULT_STATISTICS + "_" + statisticInterval + "` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                + "`success_count` INT(11),"
                + "`failed_count` INT(11),"
                + "`statistics_time` TIMESTAMP NOT NULL,"
                + "`creation_time` TIMESTAMP NOT NULL,"
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createTaskRunningTableIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_TASK_RUNNING_STATISTICS, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                createTaskRunningTable(conn);
            }
        }
    }
    
    private void createTaskRunningTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_TASK_RUNNING_STATISTICS + "` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                + "`running_count` INT(11),"
                + "`statistics_time` TIMESTAMP NOT NULL,"
                + "`creation_time` TIMESTAMP NOT NULL,"
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobRunningTableIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_RUNNING_STATISTICS, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                createJobRunningTable(conn);
            }
        }
    }
    
    private void createJobRunningTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_RUNNING_STATISTICS + "` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                + "`running_count` INT(11),"
                + "`statistics_time` TIMESTAMP NOT NULL,"
                + "`creation_time` TIMESTAMP NOT NULL,"
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }
    
    private void createJobRegisterTableIfNeeded(final Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        try (ResultSet resultSet = dbMetaData.getTables(null, null, TABLE_JOB_REGISTER_STATISTICS, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                createJobRegisterTable(conn);
            }
        }
    }
    
    private void createJobRegisterTable(final Connection conn) throws SQLException {
        String dbSchema = "CREATE TABLE `" + TABLE_JOB_REGISTER_STATISTICS + "` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                + "`registered_count` INT(11),"
                + "`statistics_time` TIMESTAMP NOT NULL,"
                + "`creation_time` TIMESTAMP NOT NULL,"
                + "PRIMARY KEY (`id`));";
        try (PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.execute();
        }
    }

    /**
     * Add task result statistics.
     *
     * @param taskResultStatistics task result statistics
     * @return add success or not
     */
    public boolean add(final TaskResultStatistics taskResultStatistics) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_TASK_RESULT_STATISTICS + "_" + taskResultStatistics.getStatisticInterval()
                + "` (`success_count`, `failed_count`, `statistics_time`, `creation_time`) VALUES (?, ?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, taskResultStatistics.getSuccessCount());
            preparedStatement.setInt(2, taskResultStatistics.getFailedCount());
            preparedStatement.setTimestamp(3, new Timestamp(taskResultStatistics.getStatisticsTime().getTime()));
            preparedStatement.setTimestamp(4, new Timestamp(taskResultStatistics.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Insert taskResultStatistics to DB error:", ex);
        }
        return result;
    }

    /**
     * Add task running statistics.
     *
     * @param taskRunningStatistics task running statistics
     * @return add success or not
     */
    public boolean add(final TaskRunningStatistics taskRunningStatistics) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_TASK_RUNNING_STATISTICS + "` (`running_count`, `statistics_time`, `creation_time`) VALUES (?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, taskRunningStatistics.getRunningCount());
            preparedStatement.setTimestamp(2, new Timestamp(taskRunningStatistics.getStatisticsTime().getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(taskRunningStatistics.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Insert taskRunningStatistics to DB error:", ex);
        }
        return result;
    }

    /**
     * Add job running statistics.
     *
     * @param jobRunningStatistics job running statistics
     * @return add success or not
     */
    public boolean add(final JobRunningStatistics jobRunningStatistics) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_RUNNING_STATISTICS + "` (`running_count`, `statistics_time`, `creation_time`) VALUES (?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, jobRunningStatistics.getRunningCount());
            preparedStatement.setTimestamp(2, new Timestamp(jobRunningStatistics.getStatisticsTime().getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(jobRunningStatistics.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Insert jobRunningStatistics to DB error:", ex);
        }
        return result;
    }

    /**
     * Add job register statistics.
     *
     * @param jobRegisterStatistics job register statistics
     * @return add success or not
     */
    public boolean add(final JobRegisterStatistics jobRegisterStatistics) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_JOB_REGISTER_STATISTICS + "` (`registered_count`, `statistics_time`, `creation_time`) VALUES (?, ?, ?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, jobRegisterStatistics.getRegisteredCount());
            preparedStatement.setTimestamp(2, new Timestamp(jobRegisterStatistics.getStatisticsTime().getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(jobRegisterStatistics.getCreationTime().getTime()));
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Insert jobRegisterStatistics to DB error:", ex);
        }
        return result;
    }

    /**
     * Find task result statistics.
     *
     * @param from from date to statistics
     * @param statisticInterval statistic interval
     * @return task result statistics
     */
    public List<TaskResultStatistics> findTaskResultStatistics(final Date from, final StatisticInterval statisticInterval) {
        List<TaskResultStatistics> result = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = String.format("SELECT id, success_count, failed_count, statistics_time, creation_time FROM %s WHERE statistics_time >= '%s' order by id ASC", 
                TABLE_TASK_RESULT_STATISTICS + "_" + statisticInterval, formatter.format(from));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                TaskResultStatistics taskResultStatistics = new TaskResultStatistics(resultSet.getLong(1), resultSet.getInt(2), resultSet.getInt(3), 
                        statisticInterval, new Date(resultSet.getTimestamp(4).getTime()), new Date(resultSet.getTimestamp(5).getTime()));
                result.add(taskResultStatistics);
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch taskResultStatistics from DB error:", ex);
        }
        return result;
    }

    /**
     * Get summed task result statistics.
     *
     * @param from from date to statistics
     * @param statisticInterval statistic interval
     * @return summed task result statistics
     */
    public TaskResultStatistics getSummedTaskResultStatistics(final Date from, final StatisticInterval statisticInterval) {
        TaskResultStatistics result = new TaskResultStatistics(0, 0, statisticInterval, new Date());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = String.format("SELECT sum(success_count), sum(failed_count) FROM %s WHERE statistics_time >= '%s'", 
                TABLE_TASK_RESULT_STATISTICS + "_" + statisticInterval, formatter.format(from));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                result = new TaskResultStatistics(resultSet.getInt(1), resultSet.getInt(2), statisticInterval, new Date());
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch summed taskResultStatistics from DB error:", ex);
        }
        return result;
    }

    /**
     * Find latest task result statistics.
     *
     * @param statisticInterval statistic interval
     * @return task result statistics
     */
    public Optional<TaskResultStatistics> findLatestTaskResultStatistics(final StatisticInterval statisticInterval) {
        TaskResultStatistics result = null;
        String sql = String.format("SELECT id, success_count, failed_count, statistics_time, creation_time FROM %s order by id DESC LIMIT 1", 
                TABLE_TASK_RESULT_STATISTICS + "_" + statisticInterval);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                result = new TaskResultStatistics(resultSet.getLong(1), resultSet.getInt(2), resultSet.getInt(3), 
                        statisticInterval, new Date(resultSet.getTimestamp(4).getTime()), new Date(resultSet.getTimestamp(5).getTime()));
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch latest taskResultStatistics from DB error:", ex);
        }
        return Optional.ofNullable(result);
    }

    /**
     * Find task running statistics.
     *
     * @param from from date to statistics
     * @return Task running statistics
     */
    public List<TaskRunningStatistics> findTaskRunningStatistics(final Date from) {
        List<TaskRunningStatistics> result = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = String.format("SELECT id, running_count, statistics_time, creation_time FROM %s WHERE statistics_time >= '%s' order by id ASC", 
                TABLE_TASK_RUNNING_STATISTICS, formatter.format(from));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                TaskRunningStatistics taskRunningStatistics = new TaskRunningStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
                result.add(taskRunningStatistics);
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch taskRunningStatistics from DB error:", ex);
        }
        return result;
    }

    /**
     * Find job running statistics.
     *
     * @param from from date to statistics
     * @return job running statistics
     */
    public List<JobRunningStatistics> findJobRunningStatistics(final Date from) {
        List<JobRunningStatistics> result = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = String.format("SELECT id, running_count, statistics_time, creation_time FROM %s WHERE statistics_time >= '%s' order by id ASC", 
                TABLE_JOB_RUNNING_STATISTICS, formatter.format(from));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                JobRunningStatistics jobRunningStatistics = new JobRunningStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
                result.add(jobRunningStatistics);
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch jobRunningStatistics from DB error:", ex);
        }
        return result;
    }

    /**
     * Find latest task running statistics.
     *
     * @return latest task running statistics
     */
    public Optional<TaskRunningStatistics> findLatestTaskRunningStatistics() {
        TaskRunningStatistics result = null;
        String sql = String.format("SELECT id, running_count, statistics_time, creation_time FROM %s order by id DESC LIMIT 1", 
                TABLE_TASK_RUNNING_STATISTICS);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                result = new TaskRunningStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch latest taskRunningStatistics from DB error:", ex);
        }
        return Optional.ofNullable(result);
    }

    /**
     * Find latest job running statistics.
     *
     * @return job running statistics
     */
    public Optional<JobRunningStatistics> findLatestJobRunningStatistics() {
        JobRunningStatistics result = null;
        String sql = String.format("SELECT id, running_count, statistics_time, creation_time FROM %s order by id DESC LIMIT 1", TABLE_JOB_RUNNING_STATISTICS);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                result = new JobRunningStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch latest jobRunningStatistics from DB error:", ex);
        }
        return Optional.ofNullable(result);
    }

    /**
     * Find job register statistics.
     *
     * @param from from date to statistics
     * @return job register statistics
     */
    public List<JobRegisterStatistics> findJobRegisterStatistics(final Date from) {
        List<JobRegisterStatistics> result = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = String.format("SELECT id, registered_count, statistics_time, creation_time FROM %s WHERE statistics_time >= '%s' order by id ASC", 
                TABLE_JOB_REGISTER_STATISTICS, formatter.format(from));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                JobRegisterStatistics jobRegisterStatistics = new JobRegisterStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
                result.add(jobRegisterStatistics);
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch jobRegisterStatistics from DB error:", ex);
        }
        return result;
    }

    /**
     * Find latest job register statistics.
     *
     * @return job register statistics
     */
    public Optional<JobRegisterStatistics> findLatestJobRegisterStatistics() {
        JobRegisterStatistics result = null;
        String sql = String.format("SELECT id, registered_count, statistics_time, creation_time FROM %s order by id DESC LIMIT 1", 
                TABLE_JOB_REGISTER_STATISTICS);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
                ) {
            while (resultSet.next()) {
                result = new JobRegisterStatistics(resultSet.getLong(1), resultSet.getInt(2), 
                        new Date(resultSet.getTimestamp(3).getTime()), new Date(resultSet.getTimestamp(4).getTime()));
            }
        } catch (final SQLException ex) {
            // TODO log failure directly to output log, consider to be configurable in the future
            log.error("Fetch latest jobRegisterStatistics from DB error:", ex);
        }
        return Optional.ofNullable(result);
    }
}
