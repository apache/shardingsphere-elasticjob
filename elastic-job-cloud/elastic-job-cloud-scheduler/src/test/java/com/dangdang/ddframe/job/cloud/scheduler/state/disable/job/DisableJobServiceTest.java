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

package com.dangdang.ddframe.job.cloud.scheduler.state.disable.job;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DisableJobServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private DisableJobService disableJobService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
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
