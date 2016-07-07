/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.state.running;

import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SlaveCacheTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        SlaveCache slaveCache = SlaveCache.getInstance(regCenter);
        ReflectionUtils.setFieldValue(slaveCache, "instance", null);
        ((Map) ReflectionUtils.getFieldValue(slaveCache, ReflectionUtils.getFieldWithName(SlaveCache.class, "RUNNING_TASKS", true))).clear();
        when(regCenter.getChildrenKeys("/state/running")).thenReturn(Collections.singletonList("test_job"));
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Collections.singletonList("test_job@-@0@-@00"));
        when(regCenter.get("/state/running/test_job/test_job@-@0@-@00")).thenReturn("slave-init-S0");
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(SlaveCache.getInstance(regCenter), is(SlaveCache.getInstance(regCenter)));
    }
    
    @Test
    public void assertInit() {
        assertThat(SlaveCache.getInstance(regCenter).load("slave-init"), is(Collections.singletonList(TaskContext.from("test_job@-@0@-@00"))));
    }
    
    @Test
    public void assertAdd() {
        SlaveCache.getInstance(regCenter).add("slave-add-S0", TaskContext.from("test_job@-@0@-@00"));
        assertThat(SlaveCache.getInstance(regCenter).load("slave-add"), is(Collections.singletonList(TaskContext.from("test_job@-@0@-@00"))));
    }
    
    @Test
    public void assertRemove() {
        SlaveCache.getInstance(regCenter).add("slave-remove-S0", TaskContext.from("test_job@-@0@-@00"));
        SlaveCache.getInstance(regCenter).add("slave-remove-S1", TaskContext.from("test_job@-@1@-@00"));
        SlaveCache.getInstance(regCenter).remove("slave-remove-S2", TaskContext.from("test_job@-@0@-@00"));
        assertThat(SlaveCache.getInstance(regCenter).load("slave-remove"), is(Collections.singletonList(TaskContext.from("test_job@-@1@-@00"))));
    }
}