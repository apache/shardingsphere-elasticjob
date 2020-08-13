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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.DagOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.DagBriefInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DagOperateAPIImplTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    private DagOperateAPI dagOperateAPI;

    @Before
    public void setUp() {
        dagOperateAPI = new DagOperateAPIImpl(regCenter);
    }

    @Test
    public void toggleDagPause() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("running");
        dagOperateAPI.toggleDagPause("testDag");
        verify(regCenter).update("/dag/testDag/states", "pause");
    }

    @Test
    public void toggleDagResume() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("pause");
        dagOperateAPI.toggleDagResume("testDag");
        verify(regCenter).update("/dag/testDag/states", "running");
    }

    @Test
    public void toggleDagStart() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("pause");
        assertFalse(dagOperateAPI.toggleDagStart("testDag"));
    }

    @Test
    public void toggleDagStop() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("pause");
        dagOperateAPI.toggleDagStop("testDag");
        verify(regCenter).update("/dag/testDag/states", "fail");
    }

    @Test
    public void toggleDagRerunWhenFail() {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("pause");
        assertFalse(dagOperateAPI.toggleDagRerunWhenFail("testDag"));
    }

    @Test
    public void getDagList() {
        when(regCenter.getChildrenKeys("/dag")).thenReturn(Lists.newArrayList("d1", "d2"));
        List<DagBriefInfo> dagList = dagOperateAPI.getDagList();
        assertThat(dagList.size(), is(2));
    }

    @Test
    public void getDagJobDependencies() {
        when(regCenter.getChildrenKeys("/dag/testDag/config")).thenReturn(Lists.newArrayList("job1", "job2", "job3"));
        when(regCenter.getDirectly("/dag/testDag/config/job1")).thenReturn("self");
        when(regCenter.getDirectly("/dag/testDag/config/job2")).thenReturn("self,job1");
        when(regCenter.getDirectly("/dag/testDag/config/job3")).thenReturn("job1,job2");
        List<DagBriefInfo> testDag = dagOperateAPI.getDagJobDependencies("testDag");
        assertThat(testDag.size(), is(3));
    }
}
