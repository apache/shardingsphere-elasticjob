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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DisableJobServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private DisableJobService disableJobService;
    
    @Before
    public void setUp() {
        disableJobService = new DisableJobService(regCenter);
    }
    
    @Test
    public void assertAdd() {
        disableJobService.add("test_job");
        verify(regCenter).isExisted("/state/disable/job/test_job");
        verify(regCenter).persist("/state/disable/job/test_job", "test_job");
    }
    
    @Test
    public void assertRemove() {
        disableJobService.remove("test_job");
        verify(regCenter).remove("/state/disable/job/test_job");
    }
    
    @Test
    public void assertIsDisabled() {
        when(regCenter.isExisted("/state/disable/job/test_job")).thenReturn(true);
        assertTrue(disableJobService.isDisabled("test_job"));
        verify(regCenter).isExisted("/state/disable/job/test_job");
    }
    
    @Test
    public void assertIsEnabled() {
        when(regCenter.isExisted("/state/disable/job/test_job")).thenReturn(false);
        assertFalse(disableJobService.isDisabled("test_job"));
        verify(regCenter).isExisted("/state/disable/job/test_job");
    }
}
