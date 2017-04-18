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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.state.failover.FailoverTaskInfo;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Condition;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Result;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.type.job.JobExecutionTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobRunningStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskResultStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningStatistics;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
import java.util.Set;

/**
 * 作业云Job的REST API.
 *
 * @author zhangliang
 * @author liguangyun
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
        Optional<JobEventRdbConfiguration> jobEventRdbConfiguration = Optional.absent();
        statisticManager = StatisticManager.getInstance(regCenter, jobEventRdbConfiguration);
    }
    
    /**
     * 初始化.
     * 
     * @param regCenter 注册中心
     * @param producerManager 生产管理器
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        CloudJobRestfulApi.regCenter = regCenter;
        CloudJobRestfulApi.producerManager = producerManager;
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudJobConfigurationGsonFactory.CloudJobConfigurationGsonTypeAdapter());
        Optional<JobEventRdbConfiguration> jobEventRdbConfig = BootstrapEnvironment.getInstance().getJobEventRdbConfiguration();
        if (jobEventRdbConfig.isPresent()) {
            jobEventRdbSearch = new JobEventRdbSearch(jobEventRdbConfig.get().getDataSource());
        } else {
            jobEventRdbSearch = null;
        }
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudJobConfiguration jobConfig) {
        producerManager.register(jobConfig);
    }
    
    /**
     * 更新作业配置.
     *
     * @param jobConfig 作业配置
     */
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudJobConfiguration jobConfig) {
        producerManager.update(jobConfig);
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    @DELETE
    @Path("/deregister")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(final String jobName) {
        producerManager.deregister(jobName);
    }
    
    /**
     * 触发一次作业.
     *
     * @param jobName 作业名称
     */
    @POST
    @Path("/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void trigger(final String jobName) {
        Optional<CloudJobConfiguration> config = configService.load(jobName);
        if (config.isPresent() && CloudJobExecutionType.DAEMON == config.get().getJobExecutionType()) {
            throw new JobSystemException("Daemon job '%s' cannot support trigger.", jobName);
        }
        facadeService.addTransient(jobName);
    }
    
    /**
     * 查询作业详情.
     *
     * @param jobName 作业名称
     * @return 作业配置对象
     */
    @GET
    @Path("/jobs/{jobName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CloudJobConfiguration detail(@PathParam("jobName") final String jobName) {
        Optional<CloudJobConfiguration> config = configService.load(jobName);
        if (config.isPresent()) {
            return config.get();
        }
        throw new JobConfigurationException("Cannot find job '%s', please check the jobName.", jobName);
    }
    
    /**
     * 查找全部作业.
     * 
     * @return 全部作业
     */
    @GET
    @Path("/jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<CloudJobConfiguration> findAllJobs() {
        return configService.loadAll();
    }
    
    /**
     * 查找运行中的全部任务.
     * 
     * @return 运行中的全部任务
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
     * 查找待运行的全部任务.
     * 
     * @return 待运行的全部任务
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
     * 查找待失效转移的全部任务.
     * 
     * @return 失效转移的全部任务
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
     * 检索作业运行轨迹.
     * 
     * @param info URL信息
     * @return 作业运行轨迹结果
     * @throws ParseException 解析异常
     */
    @GET
    @Path("events/executions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<JobExecutionEvent> findJobExecutionEvents(@Context final UriInfo info) throws ParseException {
        if (!isRdbConfigured()) {
            return new Result<>(0, Collections.<JobExecutionEvent>emptyList());
        }
        return jobEventRdbSearch.findJobExecutionEvents(buildCondition(info, new String[]{"jobName", "taskId", "ip", "isSuccess"}));
    }
    
    /**
     * 检索作业运行状态轨迹.
     * 
     * @param info URL信息
     * @return 作业运行轨迹结果
     * @throws ParseException 转换异常
     */
    @GET
    @Path("events/statusTraces")
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<JobStatusTraceEvent> findJobStatusTraceEvents(@Context final UriInfo info) throws ParseException {
        if (!isRdbConfigured()) {
            return new Result<>(0, Collections.<JobStatusTraceEvent>emptyList());
        }
        return jobEventRdbSearch.findJobStatusTraceEvents(buildCondition(info, new String[]{"jobName", "taskId", "slaveId", "source", "executionType", "state"}));
    }
    
    private boolean isRdbConfigured() {
        return null != jobEventRdbSearch;
    }
    
    private Condition buildCondition(final UriInfo info, final String[] params) throws ParseException {
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
        return new Condition(perPage, page, sort, order, startTime, endTime, fields);
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
     * 获取任务运行结果统计数据.
     * 
     * @param since 时间跨度
     * @return 任务运行结果统计数据
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
     * 获取任务运行结果统计数据.
     * 
     * @param period 时间跨度
     * @return 任务运行结果统计数据
     */
    @GET
    @Path("/statistics/tasks/results/{period}")
    @Consumes(MediaType.APPLICATION_JSON)
    public TaskResultStatistics getTaskResultStatistics(@PathParam("period") final String period) {
        if ("online".equals(period)) {
            return statisticManager.getTaskResultStatisticsSinceOnline();
        } else if ("lastWeek".equals(period)) {
            return statisticManager.getTaskResultStatisticsWeekly();
        } else if ("lastHour".equals(period)) {
            return statisticManager.findLatestTaskResultStatistics(StatisticInterval.HOUR);
        } else if ("lastMinute".equals(period)) {
            return statisticManager.findLatestTaskResultStatistics(StatisticInterval.MINUTE);
        } else {
            return new TaskResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
    }
    
    /**
     * 获取任务运行统计数据集合.
     * 
     * @param since 时间跨度
     * @return 任务运行统计数据集合
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
     * 获取作业类型统计数据.
     * 
     * @return 作业类型统计数据
     */
    @GET
    @Path("/statistics/jobs/type")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobTypeStatistics getJobTypeStatistics() {
        return statisticManager.getJobTypeStatistics();
    }
    
    /**
     * 获取作业执行类型统计数据.
     * 
     * @return 作业执行类型统计数据
     */
    @GET
    @Path("/statistics/jobs/executionType")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        return statisticManager.getJobExecutionTypeStatistics();
    }
    
    /**
     * 获取一周以来作业运行统计数据集合.
     * 
     * @param since 时间跨度
     * @return 一周以来任务运行统计数据集合
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
     * 获取自上线以来作业注册统计数据集合.
     * 
     * @return 自上线以来作业注册统计数据集合
     */
    @GET
    @Path("/statistics/jobs/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<JobRegisterStatistics> findJobRegisterStatistics() {
        return statisticManager.findJobRegisterStatisticsSinceOnline();
    }
}
