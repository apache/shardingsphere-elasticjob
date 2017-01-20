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

import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJsonConstants;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManagerFactory;
import com.dangdang.ddframe.job.cloud.scheduler.state.failover.FailoverTaskInfo;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.context.TaskContext.MetaInfo;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Condition;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Result;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.Source;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.State;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.dangdang.ddframe.job.statistics.type.job.JobExecutionTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskResultStatistics;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.mesos.SchedulerDriver;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentGetRequest;
import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobRestfulApiTest {
    
    private static RestfulServer server;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static JobEventRdbSearch jobEventRdbSearch;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        ReflectionUtils.setFieldValue(ProducerManagerFactory.class, ProducerManagerFactory.class.getDeclaredField("instance"), null);
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        regCenter = mock(CoordinatorRegistryCenter.class);
        jobEventRdbSearch = mock(JobEventRdbSearch.class);
        CloudJobRestfulApi.init(schedulerDriver, regCenter);
        server = new RestfulServer(19000);
        server.start(CloudJobRestfulApi.class.getPackage().getName(), Optional.of("console"));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
        server.stop();
    }
    
    @Before
    public void setUp() {
        reset(regCenter);
        reset(jobEventRdbSearch);
    }
    
    @Test
    public void assertRegister() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", CloudJsonConstants.getJobJson()), is(204));
        verify(regCenter).persist("/config/test_job", CloudJsonConstants.getJobJson());
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", CloudJsonConstants.getJobJson()), is(204));
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", CloudJsonConstants.getJobJson()), is(500));
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", "\"{\"jobName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(true);
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentRequest("http://127.0.0.1:19000/job/update", "PUT", CloudJsonConstants.getJobJson()), is(204));
        verify(regCenter).update("/config/test_job", CloudJsonConstants.getJobJson());
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job"), is(204));
        verify(regCenter).get("/config/test_job");
    }
    
    @Test
    public void assertTriggerWithDaemonJob() throws Exception {
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson(JobExecutionType.DAEMON));
        assertThat(sentRequest("http://127.0.0.1:19000/job/trigger", "POST", "test_job"), is(500));
    }
    
    @Test
    public void assertTriggerWithTransientJob() throws Exception {
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentRequest("http://127.0.0.1:19000/job/trigger", "POST", "test_job"), is(204));
    }
    
    @Test
    public void assertDetail() throws Exception {
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/jobs/test_job"), is(CloudJsonConstants.getJobJson()));
        verify(regCenter).get("/config/test_job");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/job/jobs/notExistedJobName", "GET", ""), is(500));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(regCenter.isExisted("/config")).thenReturn(true);
        when(regCenter.getChildrenKeys("/config")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/jobs"), is("[" + CloudJsonConstants.getJobJson() + "]"));
        verify(regCenter).isExisted("/config");
        verify(regCenter).getChildrenKeys("/config");
        verify(regCenter).get("/config/test_job");
    }
    
    @Test
    public void assertFindAllRunningTasks() throws Exception {
        RunningService runningService = new RunningService();
        TaskContext actualTaskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        runningService.add(actualTaskContext);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/tasks/running"), is(GsonFactory.getGson().toJson(Lists.newArrayList(actualTaskContext))));
    }
    
    @Test
    public void assertFindAllReadyTasks() throws Exception {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.get("/state/ready/test_job")).thenReturn("1");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("jobName", "test_job");
        expectedMap.put("times", "1");
        @SuppressWarnings("unchecked")
        Collection<Map<String, String>> expectedResult = Lists.newArrayList(expectedMap);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/tasks/ready"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(regCenter).isExisted("/state/ready");
        verify(regCenter).getChildrenKeys("/state/ready");
        verify(regCenter).get("/state/ready/test_job");
    }
    
    @Test
    public void assertFindAllFailoverTasks() throws Exception {
        when(regCenter.isExisted("/state/failover")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/failover")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.getChildrenKeys("/state/failover/test_job")).thenReturn(Lists.newArrayList("test_job@-@0"));
        String originalTaskId = UUID.randomUUID().toString();
        when(regCenter.get("/state/failover/test_job/test_job@-@0")).thenReturn(originalTaskId);
        FailoverTaskInfo expectedFailoverTask = new FailoverTaskInfo(MetaInfo.from("test_job@-@0"), originalTaskId);
        Collection<FailoverTaskInfo> expectedResult = Lists.newArrayList(expectedFailoverTask);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/tasks/failover"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(regCenter).isExisted("/state/failover");
        verify(regCenter).getChildrenKeys("/state/failover");
        verify(regCenter).getChildrenKeys("/state/failover/test_job");
        verify(regCenter).get("/state/failover/test_job/test_job@-@0");
    }
    
    @Test
    public void assertFindJobExecutionEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), null);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/events/executions"), is(GsonFactory.getGson().toJson(new Result<>(0, Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobExecutionEvents() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), jobEventRdbSearch);
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        when(jobEventRdbSearch.findJobExecutionEvents(any(Condition.class))).thenReturn(new Result<>(0, Lists.newArrayList(jobExecutionEvent)));
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/events/executions?" + buildFindJobEventsQueryParameter()), 
                is(GsonFactory.getGson().toJson(new Result<>(0, Lists.newArrayList(jobExecutionEvent)))));
        verify(jobEventRdbSearch).findJobExecutionEvents(any(Condition.class));
    }
    
    @Test
    public void assertFindJobStatusTraceEventEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), null);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/events/statusTraces"), is(GsonFactory.getGson().toJson(new Result<>(0, Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobStatusTraceEvent() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), jobEventRdbSearch);
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(
                "test-job", "fake_task_id", "fake_slave_id",  Source.LITE_EXECUTOR, ExecutionType.READY, "0", State.TASK_RUNNING, "message is empty.");
        when(jobEventRdbSearch.findJobStatusTraceEvents(any(Condition.class))).thenReturn(new Result<>(0, Lists.newArrayList(jobStatusTraceEvent)));
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/events/statusTraces?" + buildFindJobEventsQueryParameter()), 
                is(GsonFactory.getGson().toJson(new Result<>(0, Lists.newArrayList(jobStatusTraceEvent)))));
        verify(jobEventRdbSearch).findJobStatusTraceEvents(any(Condition.class));
    }
    
    private String buildFindJobEventsQueryParameter() throws UnsupportedEncodingException {
        return "per_page=10&page=1&sort=jobName&order=DESC&jobName=test_job"
                + "&startTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8") + "&endTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8");
    }
    
    @Test
    public void assertGetTaskResultStatistics() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/results"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithSinceParameter() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/results?since=last24hours"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithPathParameter() throws Exception {
        String[] paramters = {"online", "lastWeek", "lastHour", "lastMinute"};
        for (String each : paramters) {
            String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/results/" + each);
            TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
            assertThat(taskResultStatistics.getSuccessCount(), is(0));
            assertThat(taskResultStatistics.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithErrorPathParameter() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/results/errorPath");
        TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
        assertThat(taskResultStatistics.getSuccessCount(), is(0));
        assertThat(taskResultStatistics.getFailedCount(), is(0));
    }
    
    @Test
    public void assertGetJobTypeStatistics() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/jobs/type");
        JobTypeStatistics jobTypeStatistics = GsonFactory.getGson().fromJson(result, JobTypeStatistics.class);
        assertThat(jobTypeStatistics.getSimpleJobCount(), is(0));
        assertThat(jobTypeStatistics.getDataflowJobCount(), is(0));
        assertThat(jobTypeStatistics.getScriptJobCount(), is(0));
    }
    
    @Test
    public void assertGetJobExecutionTypeStatistics() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/jobs/executionType");
        JobExecutionTypeStatistics jobExecutionTypeStatistics = GsonFactory.getGson().fromJson(result, JobExecutionTypeStatistics.class);
        assertThat(jobExecutionTypeStatistics.getDaemonJobCount(), is(0));
        assertThat(jobExecutionTypeStatistics.getTransientJobCount(), is(0));
    }
    
    
    @Test
    public void assertFindTaskRunningStatistics() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWeekly() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/tasks/running?since=lastWeek"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatistics() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/jobs/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWeekly() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/jobs/running?since=lastWeek"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsSinceOnline() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/jobs/register"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
}
