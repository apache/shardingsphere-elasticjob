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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.FacadeService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.search.JobEventRdbSearch;
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
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
@Path("/job")
@Slf4j
public final class CloudJobRestfulApi {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static JobEventRdbSearch jobEventRdbSearch;
    
    private static ProducerManager producerManager;
    
    private final CloudJobConfigurationService configService;
    
    private final FacadeService facadeService;
    
    private final StatisticManager statisticManager;
    
    public CloudJobRestfulApi() {
        Preconditions.checkNotNull(regCenter);
        configService = new CloudJobConfigurationService(regCenter);
        facadeService = new FacadeService(regCenter);
        statisticManager = StatisticManager.getInstance(regCenter, null);
    }
    
    /**
     * Init.
     * 
     * @param regCenter registry center
     * @param producerManager producer manager
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        CloudJobRestfulApi.regCenter = regCenter;
        CloudJobRestfulApi.producerManager = producerManager;
        Optional<TracingConfiguration> tracingConfiguration = BootstrapEnvironment.getInstance().getTracingConfiguration();
        jobEventRdbSearch = tracingConfiguration.map(tracingConfiguration1 -> new JobEventRdbSearch((DataSource) tracingConfiguration1.getStorage())).orElse(null);
    }
    
    /**
     * Register cloud job.
     * 
     * @param cloudJobConfig cloud job configuration
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.register(cloudJobConfig);
    }
    
    /**
     * Update cloud job.
     *
     * @param cloudJobConfig cloud job configuration
     */
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudJobConfigurationPOJO cloudJobConfig) {
        producerManager.update(cloudJobConfig);
    }
    
    /**
     * Deregister cloud job.
     * 
     * @param jobName job name
     */
    @DELETE
    @Path("/deregister")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(final String jobName) {
        producerManager.deregister(jobName);
    }
    
    /**
     * Check whether the cloud job is disabled or not.
     *
     * @param jobName job name
     * @return true is disabled, otherwise not
     */
    @GET
    @Path("/{jobName}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isDisabled(@PathParam("jobName") final String jobName) {
        return facadeService.isJobDisabled(jobName);
    }
    
    /**
     * Enable cloud job.
     *
     * @param jobName job name
     */
    @POST
    @Path("/{jobName}/enable")
    public void enable(@PathParam("jobName") final String jobName) {
        Optional<CloudJobConfigurationPOJO> configOptional = configService.load(jobName);
        if (configOptional.isPresent()) {
            facadeService.enableJob(jobName);
            producerManager.reschedule(jobName);
        }
    }
    
    /**
     * Disable cloud job.
     *
     * @param jobName job name
     */
    @POST
    @Path("/{jobName}/disable")
    public void disable(@PathParam("jobName") final String jobName) {
        if (configService.load(jobName).isPresent()) {
            facadeService.disableJob(jobName);
            producerManager.unschedule(jobName);
        }
    }
    
    /**
     * Trigger job once.
     *
     * @param jobName job name
     */
    @POST
    @Path("/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void trigger(final String jobName) {
        Optional<CloudJobConfigurationPOJO> config = configService.load(jobName);
        if (config.isPresent() && CloudJobExecutionType.DAEMON == config.get().getJobExecutionType()) {
            throw new JobSystemException("Daemon job '%s' cannot support trigger.", jobName);
        }
        facadeService.addTransient(jobName);
    }
    
    /**
     * Query job detail.
     *
     * @param jobName job name
     * @return the job detail
     */
    @GET
    @Path("/jobs/{jobName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response detail(@PathParam("jobName") final String jobName) {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cloudJobConfig.get()).build();
    }
    
    /**
     * Find all jobs.
     * 
     * @return all jobs
     */
    @GET
    @Path("/jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<CloudJobConfigurationPOJO> findAllJobs() {
        return configService.loadAll();
    }
    
    /**
     * Find all running tasks.
     * 
     * @return all running tasks
     */
    @GET
    @Path("tasks/running")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<TaskContext> findAllRunningTasks() {
        List<TaskContext> result = new LinkedList<>();
        for (Set<TaskContext> each : facadeService.getAllRunningTasks().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    /**
     * Find all ready tasks.
     * 
     * @return collection of all ready tasks
     */
    @GET
    @Path("tasks/ready")
    @Consumes(MediaType.APPLICATION_JSON)
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
     * 
     * @return collection of all the failover tasks
     */
    @GET
    @Path("tasks/failover")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<FailoverTaskInfo> findAllFailoverTasks() {
        List<FailoverTaskInfo> result = new LinkedList<>();
        for (Collection<FailoverTaskInfo> each : facadeService.getAllFailoverTasks().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    /**
     * Find job execution events.
     * 
     * @param info uri info
     * @return job execution event
     * @throws ParseException parse exception
     */
    @GET
    @Path("events/executions")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobEventRdbSearch.Result<JobExecutionEvent> findJobExecutionEvents(@Context final UriInfo info) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.<JobExecutionEvent>emptyList());
        }
        return jobEventRdbSearch.findJobExecutionEvents(buildCondition(info, new String[]{"jobName", "taskId", "ip", "isSuccess"}));
    }
    
    /**
     * Find job status trace events.
     * 
     * @param info uri info
     * @return job status trace event
     * @throws ParseException parse exception
     */
    @GET
    @Path("events/statusTraces")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobEventRdbSearch.Result<JobStatusTraceEvent> findJobStatusTraceEvents(@Context final UriInfo info) throws ParseException {
        if (!isRdbConfigured()) {
            return new JobEventRdbSearch.Result<>(0, Collections.<JobStatusTraceEvent>emptyList());
        }
        return jobEventRdbSearch.findJobStatusTraceEvents(buildCondition(info, new String[]{"jobName", "taskId", "slaveId", "source", "executionType", "state"}));
    }
    
    private boolean isRdbConfigured() {
        return null != jobEventRdbSearch;
    }
    
    private JobEventRdbSearch.Condition buildCondition(final UriInfo info, final String[] params) throws ParseException {
        int perPage = 10;
        int page = 1;
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("per_page"))) {
            perPage = Integer.parseInt(info.getQueryParameters().getFirst("per_page"));
        }
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("page"))) {
            page = Integer.parseInt(info.getQueryParameters().getFirst("page"));
        }
        String sort = info.getQueryParameters().getFirst("sort");
        String order = info.getQueryParameters().getFirst("order");
        Date startTime = null;
        Date endTime = null;
        Map<String, Object> fields = getQueryParameters(info, params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("startTime"))) {
            startTime = simpleDateFormat.parse(info.getQueryParameters().getFirst("startTime"));
        }
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("endTime"))) {
            endTime = simpleDateFormat.parse(info.getQueryParameters().getFirst("endTime"));
        }
        return new JobEventRdbSearch.Condition(perPage, page, sort, order, startTime, endTime, fields);
    }
    
    private Map<String, Object> getQueryParameters(final UriInfo info, final String[] params) {
        final Map<String, Object> result = new HashMap<>();
        for (String each : params) {
            if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst(each))) {
                result.put(each, info.getQueryParameters().getFirst(each));
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
    @GET
    @Path("/statistics/tasks/results")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<TaskResultStatistics> findTaskResultStatistics(@QueryParam("since") final String since) {
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
    @GET
    @Path("/statistics/tasks/results/{period}")
    @Consumes(MediaType.APPLICATION_JSON)
    public TaskResultStatistics getTaskResultStatistics(@PathParam("period") final String period) {
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
    @GET
    @Path("/statistics/tasks/running")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<TaskRunningStatistics> findTaskRunningStatistics(@QueryParam("since") final String since) {
        if ("lastWeek".equals(since)) {
            return statisticManager.findTaskRunningStatisticsWeekly();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Get job execution type statistics.
     * 
     * @return job execution statistics
     */
    @GET
    @Path("/statistics/jobs/executionType")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        return statisticManager.getJobExecutionTypeStatistics();
    }
    
    /**
     * Find job running statistics in the recent week.
     * @param since time span
     * @return collection of job running statistics in the recent week
     */
    @GET
    @Path("/statistics/jobs/running")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<JobRunningStatistics> findJobRunningStatistics(@QueryParam("since") final String since) {
        if ("lastWeek".equals(since)) {
            return statisticManager.findJobRunningStatisticsWeekly();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Find job register statistics.
     * 
     * @return collection of job register statistics since online
     */
    @GET
    @Path("/statistics/jobs/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<JobRegisterStatistics> findJobRegisterStatistics() {
        return statisticManager.findJobRegisterStatisticsSinceOnline();
    }
}
