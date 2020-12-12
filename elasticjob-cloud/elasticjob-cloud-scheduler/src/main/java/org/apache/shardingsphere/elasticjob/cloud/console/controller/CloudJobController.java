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

package org.apache.shardingsphere.elasticjob.cloud.console.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.FacadeService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverTaskInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobExecutionTypeStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.wrapper.QueryParameterMap;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.ContextPath;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Cloud job restful api.
 */
@Slf4j
@ContextPath("/api/job")
public final class CloudJobController implements RestfulController {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static JobEventRdbSearch jobEventRdbSearch;
    
    private static ProducerManager producerManager;
    
    private final CloudJobConfigurationService configService;
    
    private final FacadeService facadeService;
    
    private final StatisticManager statisticManager;
    
    public CloudJobController() {
        Preconditions.checkNotNull(regCenter);
        configService = new CloudJobConfigurationService(regCenter);
        facadeService = new FacadeService(regCenter);
        statisticManager = StatisticManager.getInstance(regCenter, null);
    }
    
    /**
     * Init.
     * @param regCenter       registry center
     * @param producerManager producer manager
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        CloudJobController.regCenter = regCenter;
        CloudJobController.producerManager = producerManager;
        Optional<TracingConfiguration<?>> tracingConfiguration = BootstrapEnvironment.getINSTANCE().getTracingConfiguration();
        jobEventRdbSearch = tracingConfiguration.map(tracingConfiguration1 -> new JobEventRdbSearch((DataSource) tracingConfiguration1.getTracingStorageConfiguration().getStorage())).orElse(null);
    }
    
    /**
     * Register cloud job.
     *
     * @param cloudJobConfig cloud job configuration
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/register")
    public boolean register(@RequestBody final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.register(cloudJobConfig);
        return true;
    }
    
    /**
     * Update cloud job.
     *
     * @param cloudJobConfig cloud job configuration
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.PUT, path = "/update")
    public boolean update(@RequestBody final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.update(cloudJobConfig);
        return true;
    }
    
    /**
     * Deregister cloud job.
     *
     * @param jobName job name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.DELETE, path = "/{jobName}/deregister")
    public boolean deregister(@Param(name = "jobName", source = ParamSource.PATH) final String jobName) {
        producerManager.deregister(jobName);
        return true;
    }
    
    /**
     * Check whether the cloud job is disabled or not.
     *
     * @param jobName job name
     * @return true is disabled, otherwise not
     */
    @Mapping(method = Http.GET, path = "/{jobName}/disable")
    public boolean isDisabled(@Param(name = "jobName", source = ParamSource.PATH) final String jobName) {
        return facadeService.isJobDisabled(jobName);
    }
    
    /**
     * Enable cloud job.
     *
     * @param jobName job name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/{jobName}/enable")
    public boolean enable(@Param(name = "jobName", source = ParamSource.PATH) final String jobName) {
        Optional<CloudJobConfigurationPOJO> configOptional = configService.load(jobName);
        if (configOptional.isPresent()) {
            facadeService.enableJob(jobName);
            producerManager.reschedule(jobName);
        }
        return true;
    }
    
    /**
     * Disable cloud job.
     *
     * @param jobName job name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/{jobName}/disable")
    public boolean disable(@Param(name = "jobName", source = ParamSource.PATH) final String jobName) {
        if (configService.load(jobName).isPresent()) {
            facadeService.disableJob(jobName);
            producerManager.unschedule(jobName);
        }
        return true;
    }
    
    /**
     * Trigger job once.
     *
     * @param jobName job name
     * @return <tt>true</tt> for operation finished.
     */
    @Mapping(method = Http.POST, path = "/trigger")
    public boolean trigger(@RequestBody final String jobName) {
        Optional<CloudJobConfigurationPOJO> config = configService.load(jobName);
        if (config.isPresent() && CloudJobExecutionType.DAEMON == config.get().getJobExecutionType()) {
            throw new JobSystemException("Daemon job '%s' cannot support trigger.", jobName);
        }
        facadeService.addTransient(jobName);
        return true;
    }
    
    /**
     * Query job detail.
     *
     * @param jobName job name
     * @return the job detail
     */
    @Mapping(method = Http.GET, path = "/jobs/{jobName}")
    public CloudJobConfigurationPOJO detail(@Param(name = "jobName", source = ParamSource.PATH) final String jobName) {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        return cloudJobConfig.orElse(null);
    }
    
    /**
     * Find all jobs.
     * @return all jobs
     */
    @Mapping(method = Http.GET, path = "/jobs")
    public Collection<CloudJobConfigurationPOJO> findAllJobs() {
        return configService.loadAll();
    }
    
    /**
     * Find all running tasks.
     * @return all running tasks
     */
    @Mapping(method = Http.GET, path = "/tasks/running")
    public Collection<TaskContext> findAllRunningTasks() {
        List<TaskContext> result = new LinkedList<>();
        for (Set<TaskContext> each : facadeService.getAllRunningTasks().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    /**
     * Find all ready tasks.
     * @return collection of all ready tasks
     */
    @Mapping(method = Http.GET, path = "/tasks/ready")
    public Collection<Map<String, String>> findAllReadyTasks() {
        Map<String, Integer> readyTasks = facadeService.getAllReadyTasks();
        List<Map<String, String>> result = new ArrayList<>(readyTasks.size());
        for (Entry<String, Integer> each : readyTasks.entrySet()) {
            Map<String, String> oneTask = new HashMap<>(2, 1);
            oneTask.put("jobName", each.getKey());
            oneTask.put("times", String.valueOf(each.getValue()));
            result.add(oneTask);
        }
        return result;
    }
    
    /**
     * Find all failover tasks.
     * @return collection of all the failover tasks
     */
    @Mapping(method = Http.GET, path = "/tasks/failover")
    public Collection<FailoverTaskInfo> findAllFailoverTasks() {
        List<FailoverTaskInfo> result = new LinkedList<>();
        for (Collection<FailoverTaskInfo> each : facadeService.getAllFailoverTasks().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    /**
     * Find job execution events.
     * @param requestParams request params
     * @return job execution event
     * @throws ParseException parse exception
     */
    @Mapping(method = Http.GET, path = "/events/executions")
    public JobEventRdbSearch.Result<JobExecutionEvent> findJobExecutionEvents(final QueryParameterMap requestParams) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.emptyList());
        }
        return jobEventRdbSearch.findJobExecutionEvents(buildCondition(requestParams.toSingleValueMap(), new String[]{"jobName", "taskId", "ip", "isSuccess"}));
    }
    
    /**
     * Find job status trace events.
     * @param requestParams request params
     * @return job status trace event
     * @throws ParseException parse exception
     */
    @Mapping(method = Http.GET, path = "/events/statusTraces")
    public JobEventRdbSearch.Result<JobStatusTraceEvent> findJobStatusTraceEvents(final QueryParameterMap requestParams) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.emptyList());
        }
        return jobEventRdbSearch.findJobStatusTraceEvents(buildCondition(requestParams.toSingleValueMap(), new String[]{"jobName", "taskId", "slaveId", "source", "executionType", "state"}));
    }
    
    private boolean isRdbConfigured() {
        return null != jobEventRdbSearch;
    }
    
    private JobEventRdbSearch.Condition buildCondition(final Map<String, String> requestParams, final String[] params) throws ParseException {
        int perPage = 10;
        int page = 1;
        if (!Strings.isNullOrEmpty(requestParams.get("per_page"))) {
            perPage = Integer.parseInt(requestParams.get("per_page"));
        }
        if (!Strings.isNullOrEmpty(requestParams.get("page"))) {
            page = Integer.parseInt(requestParams.get("page"));
        }
        String sort = requestParams.get("sort");
        String order = requestParams.get("order");
        Date startTime = null;
        Date endTime = null;
        Map<String, Object> fields = getQueryParameters(requestParams, params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(requestParams.get("startTime"))) {
            startTime = simpleDateFormat.parse(requestParams.get("startTime"));
        }
        if (!Strings.isNullOrEmpty(requestParams.get("endTime"))) {
            endTime = simpleDateFormat.parse(requestParams.get("endTime"));
        }
        return new JobEventRdbSearch.Condition(perPage, page, sort, order, startTime, endTime, fields);
    }
    
    private Map<String, Object> getQueryParameters(final Map<String, String> requestParams, final String[] params) {
        final Map<String, Object> result = new HashMap<>();
        for (String each : params) {
            if (!Strings.isNullOrEmpty(requestParams.get(each))) {
                result.put(each, requestParams.get(each));
            }
        }
        return result;
    }
    
    /**
     * Find task result statistics.
     *
     * @param since time span
     * @return task result statistics
     */
    @Mapping(method = Http.GET, path = "/statistics/tasks/results")
    public List<TaskResultStatistics> findTaskResultStatistics(@Param(name = "since", source = ParamSource.QUERY, required = false) final String since) {
        if ("last24hours".equals(since)) {
            return statisticManager.findTaskResultStatisticsDaily();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Get task result statistics.
     *
     * @param period time period
     * @return task result statistics
     */
    @Mapping(method = Http.GET, path = "/statistics/tasks/results/{period}")
    public TaskResultStatistics getTaskResultStatistics(@Param(name = "period", source = ParamSource.PATH, required = false) final String period) {
        switch (period) {
            case "online":
                return statisticManager.getTaskResultStatisticsSinceOnline();
            case "lastWeek":
                return statisticManager.getTaskResultStatisticsWeekly();
            case "lastHour":
                return statisticManager.findLatestTaskResultStatistics(StatisticInterval.HOUR);
            case "lastMinute":
                return statisticManager.findLatestTaskResultStatistics(StatisticInterval.MINUTE);
            default:
                return new TaskResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
    }
    
    /**
     * Find task running statistics.
     *
     * @param since time span
     * @return task result statistics
     */
    @Mapping(method = Http.GET, path = "/statistics/tasks/running")
    public List<TaskRunningStatistics> findTaskRunningStatistics(@Param(name = "since", source = ParamSource.QUERY, required = false) final String since) {
        if ("lastWeek".equals(since)) {
            return statisticManager.findTaskRunningStatisticsWeekly();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Get job execution type statistics.
     * @return job execution statistics
     */
    @Mapping(method = Http.GET, path = "/statistics/jobs/executionType")
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        return statisticManager.getJobExecutionTypeStatistics();
    }
    
    /**
     * Find job running statistics in the recent week.
     *
     * @param since time span
     * @return collection of job running statistics in the recent week
     */
    @Mapping(method = Http.GET, path = "/statistics/jobs/running")
    public List<JobRunningStatistics> findJobRunningStatistics(@Param(name = "since", source = ParamSource.QUERY, required = false) final String since) {
        if ("lastWeek".equals(since)) {
            return statisticManager.findJobRunningStatisticsWeekly();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Find job register statistics.
     * @return collection of job register statistics since online
     */
    @Mapping(method = Http.GET, path = "/statistics/jobs/register")
    public List<JobRegisterStatistics> findJobRegisterStatistics() {
        return statisticManager.findJobRegisterStatisticsSinceOnline();
    }
}
