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
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.api.JobDagConfiguration;
import org.apache.shardingsphere.elasticjob.infra.exception.DagRuntimeException;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DagServiceTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private DagNodeStorage dagNodeStorage;

    @Mock
    private JobEventBus jobEventBus;

    private DagService dagService;

    @Before
    public void setUp() {
        dagService = new DagService(regCenter, "testDag", dagNodeStorage);
        ReflectionUtils.setFieldValue(dagNodeStorage, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(dagService, "jobEventBus", jobEventBus);
        ReflectionUtils.setFieldValue(dagService, "dagNodeStorage", dagNodeStorage);
        ReflectionUtils.setFieldValue(dagService, "jobDagConfig",
                new JobDagConfiguration("testDag", "job1, job2", 3, 300, false, false));
        ReflectionUtils.setFieldValue(dagService, "jobName", "testJob");
    }

    @Test
    public void isDagRootJob() {
        assertFalse(dagService.isDagRootJob());
    }

    @Test
    public void getDagStates() {
        when(dagNodeStorage.currentDagStates()).thenReturn("running");
        assertThat(dagService.getDagStates(), is(DagStates.RUNNING));
    }

    @Test(expected = DagRuntimeException.class)
    public void changeDagStatesAndReGraph() {
        dagService.changeDagStatesAndReGraph();
    }

    @Test(expected = DagRuntimeException.class)
    public void checkJobDependenciesState() {
        when(dagNodeStorage.getDagJobRunStates("testJob")).thenReturn(DagJobStates.FAIL);
        dagService.checkJobDependenciesState();
    }

    @Test
    public void checkJobDependenciesStateRunning() {
        when(dagNodeStorage.getDagJobRunStates("testJob")).thenReturn(DagJobStates.RUNNING);
        when(dagNodeStorage.getJobDenpendencies()).thenReturn(new String[]{"self"});
        dagService.checkJobDependenciesState();
    }

    @Test
    public void nextShouldTriggerJob() {
        when(dagNodeStorage.getAllDagGraphJobs()).thenReturn(createAllDagGraph());
        when(dagNodeStorage.getDagJobListByState(DagJobStates.RUNNING)).thenReturn(Lists.newArrayList("job3"));
        when(dagNodeStorage.getDagJobListByState(DagJobStates.SKIP)).thenReturn(Lists.newArrayList("job1"));
        when(dagNodeStorage.getDagJobListByState(DagJobStates.SUCCESS)).thenReturn(Lists.newArrayList("job2", "testJob"));
        when(dagNodeStorage.getDagJobListByState(DagJobStates.FAIL)).thenReturn(Lists.newArrayList());
        assertThat(dagService.nextShouldTriggerJob(), is(Lists.newArrayList("job4")));
    }

    private Map<String, Set<String>> createAllDagGraph() {
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
    public void event() {
        dagService.event(CuratorCacheListener.Type.NODE_CHANGED, new ChildData("/job1/state/state", null, "running".getBytes()), new ChildData("/job1/state/state", null, "success".getBytes()));
    }
}
