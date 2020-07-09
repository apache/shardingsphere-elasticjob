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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.event.rdb.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.event.type.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverTaskInfo;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.util.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.event.type.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobExecutionTypeStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobTypeStatistics;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    @Test
    public void assertRegister() throws Exception {
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), Is.is(204));
        Mockito.verify(getRegCenter()).persist("/config/job/test_job", CloudJsonConstants.getJobJson());
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithoutApp() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), Is.is(500));
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().isExisted("/config/test_job")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), Is.is(204));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), Is.is(500));
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", "\"{\"jobName\":\"wrong_job\"}"), Is.is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(true);
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/update", "PUT", CloudJsonConstants.getJobJson()), Is.is(204));
        Mockito.verify(getRegCenter()).update("/config/job/test_job", CloudJsonConstants.getJobJson());
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job"), Is.is(204));
        Mockito.verify(getRegCenter(), Mockito.times(3)).get("/config/job/test_job");
    }
    
    @Test
    public void assertTriggerWithDaemonJob() throws Exception {
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON));
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/trigger", "POST", "test_job"), Is.is(500));
    }
    
    @Test
    public void assertTriggerWithTransientJob() throws Exception {
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/trigger", "POST", "test_job"), Is.is(204));
    }
    
    @Test
    public void assertDetail() throws Exception {
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/jobs/test_job"), Is.is(CloudJsonConstants.getJobJson()));
        Mockito.verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/jobs/notExistedJobName", "GET", ""), Is.is(404));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/jobs"), Is.is("[" + CloudJsonConstants.getJobJson() + "]"));
        Mockito.verify(getRegCenter()).isExisted("/config/job");
        Mockito.verify(getRegCenter()).getChildrenKeys("/config/job");
        Mockito.verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertFindAllRunningTasks() throws Exception {
        RunningService runningService = new RunningService(getRegCenter());
        TaskContext actualTaskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        Mockito.when(getRegCenter().get("/config/job/" + actualTaskContext.getMetaInfo().getJobName())).thenReturn(CloudJsonConstants.getJobJson());
        runningService.add(actualTaskContext);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/running"), Is.is(GsonFactory.getGson().toJson(Lists.newArrayList(actualTaskContext))));
    }
    
    @Test
    public void assertFindAllReadyTasks() throws Exception {
        Mockito.when(getRegCenter().isExisted("/state/ready")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/state/ready")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/state/ready/test_job")).thenReturn("1");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("jobName", "test_job");
        expectedMap.put("times", "1");
        @SuppressWarnings("unchecked")
        Collection<Map<String, String>> expectedResult = Lists.newArrayList(expectedMap);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/ready"), Is.is(GsonFactory.getGson().toJson(expectedResult)));
        Mockito.verify(getRegCenter()).isExisted("/state/ready");
        Mockito.verify(getRegCenter()).getChildrenKeys("/state/ready");
        Mockito.verify(getRegCenter()).get("/state/ready/test_job");
    }
    
    @Test
    public void assertFindAllFailoverTasks() throws Exception {
        Mockito.when(getRegCenter().isExisted("/state/failover")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/state/failover")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().getChildrenKeys("/state/failover/test_job")).thenReturn(Lists.newArrayList("test_job@-@0"));
        String originalTaskId = UUID.randomUUID().toString();
        Mockito.when(getRegCenter().get("/state/failover/test_job/test_job@-@0")).thenReturn(originalTaskId);
        FailoverTaskInfo expectedFailoverTask = new FailoverTaskInfo(TaskContext.MetaInfo.from("test_job@-@0"), originalTaskId);
        Collection<FailoverTaskInfo> expectedResult = Lists.newArrayList(expectedFailoverTask);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/failover"), Is.is(GsonFactory.getGson().toJson(expectedResult)));
        Mockito.verify(getRegCenter()).isExisted("/state/failover");
        Mockito.verify(getRegCenter()).getChildrenKeys("/state/failover");
        Mockito.verify(getRegCenter()).getChildrenKeys("/state/failover/test_job");
        Mockito.verify(getRegCenter()).get("/state/failover/test_job/test_job@-@0");
    }
    
    @Test
    public void assertFindJobExecutionEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), null);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/executions"), Is.is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0,
                Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobExecutionEvents() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), getJobEventRdbSearch());
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        Mockito.when(getJobEventRdbSearch().findJobExecutionEvents(Mockito.any(JobEventRdbSearch.Condition.class))).thenReturn(new JobEventRdbSearch.Result<>(0,
                Lists.newArrayList(jobExecutionEvent)));
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/executions?" + buildFindJobEventsQueryParameter()),
                Is.is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0, Lists.newArrayList(jobExecutionEvent)))));
        Mockito.verify(getJobEventRdbSearch()).findJobExecutionEvents(Mockito.any(JobEventRdbSearch.Condition.class));
    }
    
    @Test
    public void assertFindJobStatusTraceEventEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), null);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/statusTraces"), Is.is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0,
                Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobStatusTraceEvent() throws Exception {
        ReflectionUtils.setFieldValue(CloudJobRestfulApi.class, CloudJobRestfulApi.class.getDeclaredField("jobEventRdbSearch"), getJobEventRdbSearch());
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(
                "test-job", "fake_task_id", "fake_slave_id", JobStatusTraceEvent.Source.LITE_EXECUTOR, ExecutionType.READY, "0", JobStatusTraceEvent.State.TASK_RUNNING, "message is empty.");
        Mockito.when(getJobEventRdbSearch().findJobStatusTraceEvents(Mockito.any(JobEventRdbSearch.Condition.class))).thenReturn(new JobEventRdbSearch.Result<>(0,
                Lists.newArrayList(jobStatusTraceEvent)));
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/statusTraces?" + buildFindJobEventsQueryParameter()),
                Is.is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0, Lists.newArrayList(jobStatusTraceEvent)))));
        Mockito.verify(getJobEventRdbSearch()).findJobStatusTraceEvents(Mockito.any(JobEventRdbSearch.Condition.class));
    }
    
    private String buildFindJobEventsQueryParameter() throws UnsupportedEncodingException {
        return "per_page=10&page=1&sort=jobName&order=DESC&jobName=test_job"
                + "&startTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8") + "&endTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8");
    }
    
    @Test
    public void assertGetTaskResultStatistics() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithSinceParameter() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results?since=last24hours"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithPathParameter() throws Exception {
        String[] parameters = {"online", "lastWeek", "lastHour", "lastMinute"};
        for (String each : parameters) {
            String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results/" + each);
            TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
            Assert.assertThat(taskResultStatistics.getSuccessCount(), Is.is(0));
            Assert.assertThat(taskResultStatistics.getFailedCount(), Is.is(0));
        }
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithErrorPathParameter() throws Exception {
        String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results/errorPath");
        TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
        Assert.assertThat(taskResultStatistics.getSuccessCount(), Is.is(0));
        Assert.assertThat(taskResultStatistics.getFailedCount(), Is.is(0));
    }
    
    @Test
    public void assertGetJobTypeStatistics() throws Exception {
        String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/type");
        JobTypeStatistics jobTypeStatistics = GsonFactory.getGson().fromJson(result, JobTypeStatistics.class);
        Assert.assertThat(jobTypeStatistics.getSimpleJobCount(), Is.is(0));
        Assert.assertThat(jobTypeStatistics.getDataflowJobCount(), Is.is(0));
        Assert.assertThat(jobTypeStatistics.getScriptJobCount(), Is.is(0));
    }
    
    @Test
    public void assertGetJobExecutionTypeStatistics() throws Exception {
        String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/executionType");
        JobExecutionTypeStatistics jobExecutionTypeStatistics = GsonFactory.getGson().fromJson(result, JobExecutionTypeStatistics.class);
        Assert.assertThat(jobExecutionTypeStatistics.getDaemonJobCount(), Is.is(0));
        Assert.assertThat(jobExecutionTypeStatistics.getTransientJobCount(), Is.is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatistics() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/running"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWeekly() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/running?since=lastWeek"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatistics() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/running"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWeekly() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/running?since=lastWeek"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsSinceOnline() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/register"),
                Is.is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertIsDisabled() throws Exception {
        Mockito.when(getRegCenter().isExisted("/state/disable/job/test_job")).thenReturn(true);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/test_job/disable"), Is.is("true"));
    }
    
    @Test
    public void assertDisable() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/test_job/disable", "POST"), Is.is(204));
        Mockito.verify(getRegCenter()).persist("/state/disable/job/test_job", "test_job");
    }
    
    @Test
    public void assertEnable() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/test_job/enable", "POST", "test_job"), Is.is(204));
        Mockito.verify(getRegCenter()).remove("/state/disable/job/test_job");
    }
}
