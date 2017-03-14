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

package com.dangdang.ddframe.job.cloud.scheduler.state.disable.app;

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
public final class DisableAppServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private DisableAppService disableAppService;
        
    @Before
    public void setUp() throws NoSuchFieldException {
        disableAppService = new DisableAppService(regCenter);
    }
    
    @Test
    public void assertAdd() {
        disableAppService.add("test_app");
        verify(regCenter).isExisted("/state/disable/app/test_app");
        verify(regCenter).persist("/state/disable/app/test_app", "test_app");
    }
    
    @Test
    public void assertRemove() {
        disableAppService.remove("test_app");
        verify(regCenter).remove("/state/disable/app/test_app");
    }
    
    @Test
    public void assertIsDisabled() {
        when(regCenter.isExisted("/state/disable/app/test_app")).thenReturn(true);
        assertTrue(disableAppService.isDisabled("test_app"));
        verify(regCenter).isExisted("/state/disable/app/test_app");
    }
    
    @Test
    public void assertIsEnabled() {
        when(regCenter.isExisted("/state/disable/app/test_app")).thenReturn(false);
        assertFalse(disableAppService.isDisabled("test_app"));
        verify(regCenter).isExisted("/state/disable/app/test_app");
    }
}
