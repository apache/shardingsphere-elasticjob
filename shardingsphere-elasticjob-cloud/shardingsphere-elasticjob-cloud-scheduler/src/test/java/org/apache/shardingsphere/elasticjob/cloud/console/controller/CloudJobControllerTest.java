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

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.console.AbstractCloudControllerTest;
import org.apache.shardingsphere.elasticjob.cloud.console.HttpTestUtil;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverTaskInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobExecutionTypeStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CloudJobControllerTest extends AbstractCloudControllerTest {
    
    private static final String YAML = "appName: test_app\n"
            + "cpuCount: 1.0\n"
            + "cron: 0/30 * * * * ?\n"
            + "description: ''\n"
            + "disabled: false\n"
            + "failover: true\n"
            + "jobExecutionType: TRANSIENT\n"
            + "jobName: test_job\n"
            + "jobParameter: ''\n"
            + "maxTimeDiffSeconds: 0\n"
            + "memoryMB: 128.0\n"
            + "misfire: true\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingItemParameters: ''\n"
            + "shardingTotalCount: 10\n";
    
    @Test
    public void assertRegister() {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/register", CloudJsonConstants.getJobJson()), is(200));
        verify(getRegCenter()).persist("/config/job/test_job", YAML);
        HttpTestUtil.delete("http://127.0.0.1:19000/api/job/test_job/deregister");
    }
    
    @Test
    public void assertRegisterWithoutApp() {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/register", CloudJsonConstants.getJobJson()), is(500));
    }
    
    @Test
    public void assertRegisterWithExistedName() {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(null, CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/register", CloudJsonConstants.getJobJson()), is(200));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/register", CloudJsonConstants.getJobJson()), is(500));
        HttpTestUtil.delete("http://127.0.0.1:19000/api/job/test_job/deregister");
    }
    
    @Test
    public void assertRegisterWithBadRequest() {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/register", "\"{\"jobName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(true);
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.put("http://127.0.0.1:19000/api/job/update", CloudJsonConstants.getJobJson()), is(200));
        verify(getRegCenter()).update("/config/job/test_job", YAML);
        HttpTestUtil.delete("http://127.0.0.1:19000/api/job/test_job/deregister");
    }
    
    @Test
    public void assertDeregister() {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(HttpTestUtil.delete("http://127.0.0.1:19000/api/job/test_job/deregister"), is(200));
        verify(getRegCenter(), times(3)).get("/config/job/test_job");
    }
    
    @Test
    public void assertTriggerWithDaemonJob() {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/trigger", "test_job"), is(500));
    }
    
    @Test
    public void assertTriggerWithTransientJob() {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/trigger", "test_job"), is(200));
    }
    
    @Test
    public void assertDetail() {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/jobs/test_job"), is(CloudJsonConstants.getJobJson()));
        verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertFindAllJobs() {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/jobs"), is("[" + CloudJsonConstants.getJobJson() + "]"));
        verify(getRegCenter()).isExisted("/config/job");
        verify(getRegCenter()).getChildrenKeys("/config/job");
        verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertFindAllRunningTasks() {
        RunningService runningService = new RunningService(getRegCenter());
        TaskContext actualTaskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        when(getRegCenter().get("/config/job/" + actualTaskContext.getMetaInfo().getJobName())).thenReturn(CloudJsonConstants.getJobJson());
        runningService.add(actualTaskContext);
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/tasks/running"), is(HttpTestUtil.get("http://127.0.0.1:19000/api/job/tasks/running")));
    }
    
    @Test
    public void assertFindAllReadyTasks() {
        when(getRegCenter().isExisted("/state/ready")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/state/ready")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/state/ready/test_job")).thenReturn("1");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("jobName", "test_job");
        expectedMap.put("times", "1");
        Collection<Map<String, String>> expectedResult = Collections.singletonList(expectedMap);
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/tasks/ready"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(getRegCenter()).isExisted("/state/ready");
        verify(getRegCenter()).getChildrenKeys("/state/ready");
        verify(getRegCenter()).get("/state/ready/test_job");
    }
    
    @Test
    public void assertFindAllFailoverTasks() {
        when(getRegCenter().isExisted("/state/failover")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/state/failover")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().getChildrenKeys("/state/failover/test_job")).thenReturn(Collections.singletonList("test_job@-@0"));
        String originalTaskId = UUID.randomUUID().toString();
        when(getRegCenter().get("/state/failover/test_job/test_job@-@0")).thenReturn(originalTaskId);
        FailoverTaskInfo expectedFailoverTask = new FailoverTaskInfo(TaskContext.MetaInfo.from("test_job@-@0"), originalTaskId);
        Collection<FailoverTaskInfo> expectedResult = Collections.singletonList(expectedFailoverTask);
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/tasks/failover"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(getRegCenter()).isExisted("/state/failover");
        verify(getRegCenter()).getChildrenKeys("/state/failover");
        verify(getRegCenter()).getChildrenKeys("/state/failover/test_job");
        verify(getRegCenter()).get("/state/failover/test_job/test_job@-@0");
    }
    
    @Test
    public void assertFindJobExecutionEventsWhenNotConfigRDB() {
        ReflectionUtils.setStaticFieldValue(CloudJobController.class, "jobEventRdbSearch", null);
        Map<String, String> query = new HashMap<>();
        query.put("per_page", "10");
        query.put("page", "1");
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/events/executions", query), is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0,
                Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertGetTaskResultStatistics() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/results"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithSinceParameter() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/results?since=last24hours"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithPathParameter() {
        String[] parameters = {"online", "lastWeek", "lastHour", "lastMinute"};
        for (String each : parameters) {
            String result = HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/results/" + each);
            TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
            assertThat(taskResultStatistics.getSuccessCount(), is(0));
            assertThat(taskResultStatistics.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithErrorPathParameter() {
        String result = HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/results/errorPath");
        TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
        assertThat(taskResultStatistics.getSuccessCount(), is(0));
        assertThat(taskResultStatistics.getFailedCount(), is(0));
    }
    
    @Test
    public void assertGetJobExecutionTypeStatistics() {
        String result = HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/jobs/executionType");
        JobExecutionTypeStatistics jobExecutionTypeStatistics = GsonFactory.getGson().fromJson(result, JobExecutionTypeStatistics.class);
        assertThat(jobExecutionTypeStatistics.getDaemonJobCount(), is(0));
        assertThat(jobExecutionTypeStatistics.getTransientJobCount(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatistics() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWeekly() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/tasks/running?since=lastWeek"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatistics() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/jobs/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWeekly() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/jobs/running?since=lastWeek"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsSinceOnline() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/statistics/jobs/register"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertIsDisabled() {
        when(getRegCenter().isExisted("/state/disable/job/test_job")).thenReturn(true);
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/job/test_job/disable"), is("true"));
    }
    
    @Test
    public void assertDisable() {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/test_job/disable"), is(200));
        verify(getRegCenter()).persist("/state/disable/job/test_job", "test_job");
    }
    
    @Test
    public void assertEnable() {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/job/test_job/enable", "test_job"), is(200));
        verify(getRegCenter()).remove("/state/disable/job/test_job");
    }
}
