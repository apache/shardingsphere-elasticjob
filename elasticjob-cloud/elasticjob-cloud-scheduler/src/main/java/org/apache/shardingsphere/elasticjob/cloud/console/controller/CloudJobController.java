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
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationGsonFactory;
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
import org.apache.shardingsphere.elasticjob.cloud.util.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cloud job restful api.
 */
@Slf4j
@RestController
@RequestMapping("/job")
public final class CloudJobController {
    
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
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudAppConfigurationGsonFactory.CloudAppConfigurationGsonTypeAdapter());
        Optional<TracingConfiguration> tracingConfiguration = BootstrapEnvironment.getInstance().getTracingConfiguration();
        jobEventRdbSearch = tracingConfiguration.map(tracingConfiguration1 -> new JobEventRdbSearch((DataSource) tracingConfiguration1.getStorage())).orElse(null);
    }
    
    /**
     * Register cloud job.
     * @param cloudJobConfig cloud job configuration
     */
    @PostMapping("/register")
    public void register(@RequestBody final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.register(cloudJobConfig);
    }
    
    /**
     * Update cloud job.
     * @param cloudJobConfig cloud job configuration
     */
    @PutMapping("/update")
    public void update(@RequestBody final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.update(cloudJobConfig);
    }
    
    /**
     * Deregister cloud job.
     * @param jobName job name
     */
    @DeleteMapping("/{jobName}/deregister")
    public void deregister(@PathVariable final String jobName) {
        producerManager.deregister(jobName);
    }
    
    /**
     * Check whether the cloud job is disabled or not.
     * @param jobName job name
     * @return true is disabled, otherwise not
     */
    @GetMapping("/{jobName}/disable")
    public boolean isDisabled(@PathVariable("jobName") final String jobName) {
        return facadeService.isJobDisabled(jobName);
    }
    
    /**
     * Enable cloud job.
     * @param jobName job name
     */
    @PostMapping("/{jobName}/enable")
    public void enable(@PathVariable("jobName") final String jobName) {
        Optional<CloudJobConfigurationPOJO> configOptional = configService.load(jobName);
        if (configOptional.isPresent()) {
            facadeService.enableJob(jobName);
            producerManager.reschedule(jobName);
        }
    }
    
    /**
     * Disable cloud job.
     * @param jobName job name
     */
    @PostMapping("/{jobName}/disable")
    public void disable(@PathVariable("jobName") final String jobName) {
        if (configService.load(jobName).isPresent()) {
            facadeService.disableJob(jobName);
            producerManager.unschedule(jobName);
        }
    }
    
    /**
     * Trigger job once.
     * @param jobName job name
     */
    @PostMapping("/trigger")
    public void trigger(@RequestBody final String jobName) {
        Optional<CloudJobConfigurationPOJO> config = configService.load(jobName);
        if (config.isPresent() && CloudJobExecutionType.DAEMON == config.get().getJobExecutionType()) {
            throw new JobSystemException("Daemon job '%s' cannot support trigger.", jobName);
        }
        facadeService.addTransient(jobName);
    }
    
    /**
     * Query job detail.
     * @param jobName job name
     * @return the job detail
     */
    @GetMapping("/jobs/{jobName}")
    public CloudJobConfigurationPOJO detail(@PathVariable("jobName") final String jobName) {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        return cloudJobConfig.orElse(null);
    }
    
    /**
     * Find all jobs.
     * @return all jobs
     */
    @GetMapping("/jobs")
    public Collection<CloudJobConfigurationPOJO> findAllJobs() {
        return configService.loadAll();
    }
    
    /**
     * Find all running tasks.
     * @return all running tasks
     */
    @GetMapping("tasks/running")
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
    @GetMapping("tasks/ready")
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
    @GetMapping("tasks/failover")
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
    @GetMapping("events/executions")
    public JobEventRdbSearch.Result<JobExecutionEvent> findJobExecutionEvents(@RequestParam final MultiValueMap<String, String> requestParams) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.<JobExecutionEvent>emptyList());
        }
        return jobEventRdbSearch.findJobExecutionEvents(buildCondition(requestParams, new String[]{"jobName", "taskId", "ip", "isSuccess"}));
    }
    
    /**
     * Find job status trace events.
     * @param requestParams request params
     * @return job status trace event
     * @throws ParseException parse exception
     */
    @GetMapping("events/statusTraces")
    public JobEventRdbSearch.Result<JobStatusTraceEvent> findJobStatusTraceEvents(@RequestParam final MultiValueMap<String, String> requestParams) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.<JobStatusTraceEvent>emptyList());
        }
        return jobEventRdbSearch.findJobStatusTraceEvents(buildCondition(requestParams, new String[]{"jobName", "taskId", "slaveId", "source", "executionType", "state"}));
    }
    
    private boolean isRdbConfigured() {
        return null != jobEventRdbSearch;
    }
    
    private JobEventRdbSearch.Condition buildCondition(final MultiValueMap<String, String> requestParams, final String[] params) throws ParseException {
        int perPage = 10;
        int page = 1;
        if (!Strings.isNullOrEmpty(requestParams.getFirst("per_page"))) {
            perPage = Integer.parseInt(requestParams.getFirst("per_page"));
        }
        if (!Strings.isNullOrEmpty(requestParams.getFirst("page"))) {
            page = Integer.parseInt(requestParams.getFirst("page"));
        }
        String sort = requestParams.getFirst("sort");
        String order = requestParams.getFirst("order");
        Date startTime = null;
        Date endTime = null;
        Map<String, Object> fields = getQueryParameters(requestParams, params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(requestParams.getFirst("startTime"))) {
            startTime = simpleDateFormat.parse(requestParams.getFirst("startTime"));
        }
        if (!Strings.isNullOrEmpty(requestParams.getFirst("endTime"))) {
            endTime = simpleDateFormat.parse(requestParams.getFirst("endTime"));
        }
        return new JobEventRdbSearch.Condition(perPage, page, sort, order, startTime, endTime, fields);
    }
    
    private Map<String, Object> getQueryParameters(final MultiValueMap<String, String> requestParams, final String[] params) {
        final Map<String, Object> result = new HashMap<>();
        for (String each : params) {
            if (!Strings.isNullOrEmpty(requestParams.getFirst(each))) {
                result.put(each, requestParams.getFirst(each));
            }
        }
        return result;
    }
    
    /**
     * Find task result statistics.
     * @param since time span
     * @return task result statistics
     */
    @GetMapping("/statistics/tasks/results")
    public List<TaskResultStatistics> findTaskResultStatistics(@RequestParam(value = "since", required = false) final String since) {
        if ("last24hours".equals(since)) {
            return statisticManager.findTaskResultStatisticsDaily();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Get task result statistics.
     * @param period time period
     * @return task result statistics
     */
    @GetMapping("/statistics/tasks/results/{period}")
    public TaskResultStatistics getTaskResultStatistics(@PathVariable(value = "period", required = false) final String period) {
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
     * @param since time span
     * @return task result statistics
     */
    @GetMapping("/statistics/tasks/running")
    public List<TaskRunningStatistics> findTaskRunningStatistics(@RequestParam(value = "since", required = false) final String since) {
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
    @GetMapping("/statistics/jobs/executionType")
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        return statisticManager.getJobExecutionTypeStatistics();
    }
    
    /**
     * Find job running statistics in the recent week.
     * @param since time span
     * @return collection of job running statistics in the recent week
     */
    @GetMapping("/statistics/jobs/running")
    public List<JobRunningStatistics> findJobRunningStatistics(@RequestParam(value = "since", required = false) final String since) {
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
    @GetMapping("/statistics/jobs/register")
    public List<JobRegisterStatistics> findJobRegisterStatistics() {
        return statisticManager.findJobRegisterStatisticsSinceOnline();
    }
}
