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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app;

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
public final class DisableAppServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private DisableAppService disableAppService;
        
    @Before
    public void setUp() {
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
