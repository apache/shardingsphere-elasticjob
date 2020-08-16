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

package org.apache.shardingsphere.elasticjob.lite.internal.dag;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.lite.internal.state.JobStateEnum;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DagNodeStorageTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    private DagNodeStorage dagNodeStorage;

    @Before
    public void setUp() {
        dagNodeStorage = new DagNodeStorage(regCenter, "testDag", "testJob");
        ReflectionUtils.setFieldValue(dagNodeStorage, "regCenter", regCenter);
    }

    private Map<String, Set<String>> createAllDagNode() {
        Map<String, Set<String>> allDagNode = new HashMap<>(6);
        allDagNode.put("testJob", Sets.newSet("self"));
        allDagNode.put("job1", Sets.newSet("testJob"));
        allDagNode.put("job2", Sets.newSet("testJob"));
        allDagNode.put("job3", Sets.newSet("testJob", "job1"));
        allDagNode.put("job4", Sets.newSet("job1", "job2"));
        allDagNode.put("job5", Sets.newSet("job3", "job4"));
        return allDagNode;
    }

    @Test
    public void persistDagConfig() {
        dagNodeStorage.persistDagConfig("value");
        verify(regCenter).persist("/dag/testDag/config/testJob", "value");
    }

    @Test
    public void pathOfJobNodeState() {
        String s = dagNodeStorage.pathOfJobNodeState();
        assertThat(s, is("/testJob/state/state"));
    }

    @Test
    public void initDagGraph() {
        when(regCenter.isExisted("/dag/testDag/graph")).thenReturn(true);
        dagNodeStorage.initDagGraph(createAllDagNode(), "batch-foo-no");
        verify(regCenter).remove("/dag/testDag/graph");
        verify(regCenter).persist("/dag/testDag/graph", "batch-foo-no");
    }

    @Test
    public void currentDagBatchNo() {
        when(regCenter.getDirectly("/dag/testDag/graph")).thenReturn("batch-foo-no");
        assertThat(dagNodeStorage.currentDagBatchNo(), is("batch-foo-no"));
        verify(regCenter).getDirectly("/dag/testDag/graph");
    }

    @Test
    public void currentDagStates() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("fail");
        assertThat(dagNodeStorage.currentDagStates(), is("fail"));
        verify(regCenter).getDirectly("/dag/testDag/states");
    }

    @Test
    public void updateDagStates() {
        dagNodeStorage.updateDagStates(DagStates.SUCCESS);
        verify(regCenter).update("/dag/testDag/states", "success");
    }

    @Test
    public void updateDagJobStatesRunning() {
        dagNodeStorage.updateDagJobStates(JobStateEnum.RUNNING);
        verify(regCenter, times(1)).persist(eq("/dag/testDag/running/testJob"), anyString());
    }

    @Test
    public void updateDagJobStatesSkip() {
        dagNodeStorage.updateDagJobStates(JobStateEnum.SKIP);
        verify(regCenter).remove("/dag/testDag/running/testJob");
        verify(regCenter, times(1)).persist(eq("/dag/testDag/skip/testJob"), anyString());
    }

    @Test
    public void updateDagJobStatesSuccess() {
        dagNodeStorage.updateDagJobStates(JobStateEnum.SUCCESS);
        verify(regCenter).remove("/dag/testDag/running/testJob");
        verify(regCenter, times(1)).persist(eq("/dag/testDag/success/testJob"), anyString());
    }

    @Test
    public void updateDagJobStatesFail() {
        dagNodeStorage.updateDagJobStates(JobStateEnum.FAIL);
        verify(regCenter).remove("/dag/testDag/running/testJob");
        verify(regCenter, times(1)).persist(eq("/dag/testDag/fail/testJob"), anyString());
    }

    @Test
    public void getAllDagConfigJobs() {
        when(regCenter.getChildrenKeys("/dag/testDag/config")).thenReturn(Lists.newArrayList("job1", "job2", "job3"));
        when(regCenter.getDirectly("/dag/testDag/config/job1")).thenReturn("self");
        when(regCenter.getDirectly("/dag/testDag/config/job2")).thenReturn("self,job1");
        when(regCenter.getDirectly("/dag/testDag/config/job3")).thenReturn("job1,job2");
        Map<String, Set<String>> allDagConfigJobs = dagNodeStorage.getAllDagConfigJobs();
        assertThat(allDagConfigJobs.size(), is(3));
    }

    @Test
    public void getAllDagGraphJobs() {
        when(regCenter.getChildrenKeys("/dag/testDag/graph")).thenReturn(Lists.newArrayList("job1", "job2", "job3"));
        when(regCenter.getDirectly("/dag/testDag/graph/job1")).thenReturn("self");
        when(regCenter.getDirectly("/dag/testDag/graph/job2")).thenReturn("self,job1");
        when(regCenter.getDirectly("/dag/testDag/graph/job3")).thenReturn("job1,job2");
        Map<String, Set<String>> allDagGraphJobs = dagNodeStorage.getAllDagGraphJobs();
        assertThat(allDagGraphJobs.size(), is(3));
    }

    @Test
    public void getDagJobListByState() {
        when(regCenter.getChildrenKeys("/dag/testDag/running")).thenReturn(Lists.newArrayList("job1", "job2"));
        when(regCenter.getChildrenKeys("/dag/testDag/success")).thenReturn(Lists.newArrayList("job1", "job2"));
        when(regCenter.getChildrenKeys("/dag/testDag/fail")).thenReturn(Lists.newArrayList("job1", "job2"));
        when(regCenter.getChildrenKeys("/dag/testDag/skip")).thenReturn(Lists.newArrayList("job1", "job2"));
        when(regCenter.getChildrenKeys("/dag/testDag/retry")).thenReturn(Lists.newArrayList("job1", "job2"));
        assertThat(dagNodeStorage.getDagJobListByState(DagJobStates.RUNNING).size(), is(2));
        assertThat(dagNodeStorage.getDagJobListByState(DagJobStates.SUCCESS).size(), is(2));
        assertThat(dagNodeStorage.getDagJobListByState(DagJobStates.FAIL).size(), is(2));
        assertThat(dagNodeStorage.getDagJobListByState(DagJobStates.SKIP).size(), is(2));
        assertThat(dagNodeStorage.getDagJobListByState(DagJobStates.RETRY).size(), is(2));
    }

    @Test
    public void getJobDenpendencies() {
        when(regCenter.get("/dag/testDag/graph/testJob")).thenReturn("job1,job2");
        String[] jobDenpendencies = dagNodeStorage.getJobDenpendencies();
        assertThat(jobDenpendencies, is(new String[]{"job1", "job2"}));
    }

    @Test
    public void getDagJobRunStates() {
        when(regCenter.isExisted("/dag/testDag/success/job1")).thenReturn(true);
        when(regCenter.isExisted("/dag/testDag/fail/job2")).thenReturn(true);
        when(regCenter.isExisted("/dag/testDag/running/job3")).thenReturn(true);
        when(regCenter.isExisted("/dag/testDag/skip/job4")).thenReturn(true);
        assertThat(dagNodeStorage.getDagJobRunStates("job1"), is(DagJobStates.SUCCESS));
        assertThat(dagNodeStorage.getDagJobRunStates("job2"), is(DagJobStates.FAIL));
        assertThat(dagNodeStorage.getDagJobRunStates("job3"), is(DagJobStates.RUNNING));
        assertThat(dagNodeStorage.getDagJobRunStates("job4"), is(DagJobStates.SKIP));
    }

    @Test
    public void triggerJobWhenTrigged() {
        when(regCenter.isExisted("/dag/testDag/running/job1")).thenReturn(true);
        dagNodeStorage.triggerJob("job1");
    }

    @Test
    public void triggerJob() {
        when(regCenter.isExisted("/dag/testDag/running/job1")).thenReturn(false);
        when(regCenter.isExisted("/dag/testDag/success/job1")).thenReturn(false);
        when(regCenter.isExisted("/dag/testDag/fail/job1")).thenReturn(false);
        dagNodeStorage.triggerJob("job1");
    }

    @Test
    public void triggerRetryJob() {
        dagNodeStorage.triggerRetryJob();
    }

    @Test
    public void getJobRetryTimes() {
        when(regCenter.getDirectly("/dag/testDag/graph/testJob/retry")).thenReturn("3");
        assertThat(dagNodeStorage.getJobRetryTimes(), is(3));
    }

    @Test
    public void updateJobRetryTimes() {
        dagNodeStorage.updateJobRetryTimes(3);
        verify(regCenter).persist("/dag/testDag/retry/testJob", "");
        verify(regCenter).persist("/dag/testDag/graph/testJob/retry", "3");
    }

    @Test
    public void removeFailJob() {
        dagNodeStorage.removeFailJob("job1");
        verify(regCenter).remove("/dag/testDag/fail/job1");
    }

    @Test
    public void getAllDags() {
        dagNodeStorage.getAllDags();
        verify(regCenter).getChildrenKeys("/dag");
    }

}
