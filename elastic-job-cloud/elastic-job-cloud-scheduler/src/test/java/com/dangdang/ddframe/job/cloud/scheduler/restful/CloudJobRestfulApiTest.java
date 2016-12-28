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
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.context.TaskContext.MetaInfo;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Condition;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Result;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.dangdang.ddframe.job.statistics.type.job.JobExecutionTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningResultStatistics;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.apache.mesos.SchedulerDriver;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobRestfulApiTest {
    
    private static RestfulServer server;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static Optional<JobEventRdbSearch> jobEventRdbSearch;
    
    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUpClass() throws Exception {
        ReflectionUtils.setFieldValue(ProducerManagerFactory.class, ProducerManagerFactory.class.getDeclaredField("instance"), null);
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        regCenter = mock(CoordinatorRegistryCenter.class);
        jobEventRdbSearch = mock(Optional.class);
        CloudJobRestfulApi.init(schedulerDriver, regCenter, jobEventRdbSearch);
        server = new RestfulServer(19000);
        server.start(CloudJobRestfulApi.class.getPackage().getName());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
        server.stop();
    }
    
    @SuppressWarnings("unchecked")
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
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/detail?jobName=test_job"), is(CloudJsonConstants.getJobJson()));
        verify(regCenter).get("/config/test_job");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/job/detail?jobName=notExistedJobName", "GET", ""), is(500));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(regCenter.isExisted("/config")).thenReturn(true);
        when(regCenter.getChildrenKeys("/config")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.get("/config/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/allJobs"), is("[" + CloudJsonConstants.getJobJson() + "]"));
        verify(regCenter).isExisted("/config");
        verify(regCenter).getChildrenKeys("/config");
        verify(regCenter).get("/config/test_job");
    }
    
    @Test
    public void assertFindAllRunningTasks() throws Exception {
        RunningService runningService = new RunningService();
        TaskContext actualTaskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        runningService.add(actualTaskContext);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/task/runningTasks"), is(GsonFactory.getGson().toJson(Lists.newArrayList(actualTaskContext))));
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
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/task/readyTasks"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(regCenter).isExisted("/state/ready");
        verify(regCenter).getChildrenKeys("/state/ready");
        verify(regCenter).get("/state/ready/test_job");
    }
    
    @Test
    public void assertFindAllFailoverTasks() throws Exception {
        when(regCenter.isExisted("/state/failover")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/failover")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.getChildrenKeys("/state/failover/test_job")).thenReturn(Lists.newArrayList("test_job@-@0"));
        String actualOriginalTaskId = UUID.randomUUID().toString();
        when(regCenter.get("/state/failover/test_job/test_job@-@0")).thenReturn(actualOriginalTaskId);
        String expectedOriginalTaskId = actualOriginalTaskId;
        FailoverTaskInfo expectedFailoverTask = new FailoverTaskInfo(MetaInfo.from("test_job@-@0"), expectedOriginalTaskId);
        Collection<FailoverTaskInfo> expectedResult = Lists.newArrayList(expectedFailoverTask);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/task/failoverTasks"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(regCenter).isExisted("/state/failover");
        verify(regCenter).getChildrenKeys("/state/failover");
        verify(regCenter).getChildrenKeys("/state/failover/test_job");
        verify(regCenter).get("/state/failover/test_job/test_job@-@0");
    }
    
    @Test
    public void assertFindJobExecutionEventsWhenNotConfigRDB() throws Exception {
        when(jobEventRdbSearch.isPresent()).thenReturn(false);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/event/executionEvents"), is(GsonFactory.getGson().toJson(new Result<JobExecutionEvent>(0, Collections.<JobExecutionEvent>emptyList()))));
        verify(jobEventRdbSearch).isPresent();
    }
    
    @Test
    public void assertFindJobExecutionEvents() throws Exception {
        when(jobEventRdbSearch.isPresent()).thenReturn(true);
        JobEventRdbSearch mockJobEventRdbSearch = mock(JobEventRdbSearch.class);
        when(jobEventRdbSearch.get()).thenReturn(mockJobEventRdbSearch);
        when(mockJobEventRdbSearch.findJobExecutionEvents(any(Condition.class))).thenReturn(new Result<JobExecutionEvent>(0, Collections.<JobExecutionEvent>emptyList()));
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/event/executionEvents?" + buildFindJobEventsQueryParameter()), 
                is(GsonFactory.getGson().toJson(new Result<JobExecutionEvent>(0, Collections.<JobExecutionEvent>emptyList()))));
        verify(jobEventRdbSearch).isPresent();
        verify(jobEventRdbSearch).get();
        verify(mockJobEventRdbSearch).findJobExecutionEvents(any(Condition.class));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWhenNotConfigRDB() throws Exception {
        when(jobEventRdbSearch.isPresent()).thenReturn(false);
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/event/statusTraceEvents"), 
                is(GsonFactory.getGson().toJson(new Result<JobStatusTraceEvent>(0, Collections.<JobStatusTraceEvent>emptyList()))));
        verify(jobEventRdbSearch).isPresent();
    }
    
    @Test
    public void assertFindJobStatusTraceEvents() throws Exception {
        when(jobEventRdbSearch.isPresent()).thenReturn(true);
        JobEventRdbSearch mockJobEventRdbSearch = mock(JobEventRdbSearch.class);
        when(jobEventRdbSearch.get()).thenReturn(mockJobEventRdbSearch);
        when(mockJobEventRdbSearch.findJobStatusTraceEvents(any(Condition.class))).thenReturn(new Result<JobStatusTraceEvent>(0, Collections.<JobStatusTraceEvent>emptyList()));
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/event/statusTraceEvents?" + buildFindJobEventsQueryParameter()), 
                is(GsonFactory.getGson().toJson(new Result<JobExecutionEvent>(0, Collections.<JobExecutionEvent>emptyList()))));
        verify(jobEventRdbSearch).isPresent();
        verify(jobEventRdbSearch).get();
        verify(mockJobEventRdbSearch).findJobStatusTraceEvents(any(Condition.class));
    }
    
    private String buildFindJobEventsQueryParameter() throws UnsupportedEncodingException {
        return "pageSize=10&pageNumber=1&sortName=jobName&sortOrder=DESC&jobName=test_job"
                + "&startTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8") + "&endTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8");
    }
    
    @Test
    public void assertGetTaskRunningResultStatisticsWeekly() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/task/runningResult/weekly");
        TaskRunningResultStatistics taskRunningResultStatistics = GsonFactory.getGson().fromJson(result, TaskRunningResultStatistics.class);
        assertThat(taskRunningResultStatistics.getSuccessCount(), is(0));
        assertThat(taskRunningResultStatistics.getFailedCount(), is(0));
    }
    
    @Test
    public void assertGetTaskRunningResultStatisticsSinceOnline() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/task/runningResult/sinceOnline");
        TaskRunningResultStatistics taskRunningResultStatistics = GsonFactory.getGson().fromJson(result, TaskRunningResultStatistics.class);
        assertThat(taskRunningResultStatistics.getSuccessCount(), is(0));
        assertThat(taskRunningResultStatistics.getFailedCount(), is(0));
    }
    
    @Test
    public void assertGetJobTypeStatistics() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/job/type/current");
        JobTypeStatistics jobTypeStatistics = GsonFactory.getGson().fromJson(result, JobTypeStatistics.class);
        assertThat(jobTypeStatistics.getSimpleJobCount(), is(0));
        assertThat(jobTypeStatistics.getDataflowJobCount(), is(0));
        assertThat(jobTypeStatistics.getScriptJobCount(), is(0));
    }
    
    @Test
    public void assertGetJobExecutionTypeStatistics() throws Exception {
        String result = sentGetRequest("http://127.0.0.1:19000/job/statistics/job/executionType/current");
        JobExecutionTypeStatistics jobExecutionTypeStatistics = GsonFactory.getGson().fromJson(result, JobExecutionTypeStatistics.class);
        assertThat(jobExecutionTypeStatistics.getDaemonJobCount(), is(0));
        assertThat(jobExecutionTypeStatistics.getTransientJobCount(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWeekly() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/task/running/weekly"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWeekly() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/job/running/weekly"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsSinceOnline() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/job/statistics/job/register/sinceOnline"), 
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    private static int sentRequest(final String url, final String method, final String content) throws Exception {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            ContentExchange contentExchange = new ContentExchange();
            contentExchange.setMethod(method);
            contentExchange.setRequestContentType(MediaType.APPLICATION_JSON);
            contentExchange.setRequestContent(new ByteArrayBuffer(content.getBytes("UTF-8")));
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            contentExchange.setURL(url);
            httpClient.send(contentExchange);
            contentExchange.waitForDone();
            return contentExchange.getResponseStatus();
        } finally {
            httpClient.stop();
        }
    }
    
    private static String sentGetRequest(final String url) throws Exception {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            ContentExchange contentExchange = new ContentExchange();
            contentExchange.setMethod("GET");
            contentExchange.setRequestContentType(MediaType.APPLICATION_JSON);
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            contentExchange.setURL(url);
            httpClient.send(contentExchange);
            contentExchange.waitForDone();
            return contentExchange.getResponseContent();
        } finally {
            httpClient.stop();
        }
    }
}
