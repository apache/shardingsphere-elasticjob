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

package com.dangdang.ddframe.job.cloud.scheduler.ha;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FrameworkIDServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    private FrameworkIDService frameworkIDService;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        frameworkIDService = new FrameworkIDService(registryCenter);
    }
    
    @Test
    public void assertFetch() throws Exception {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("1");
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        assertTrue(frameworkIDOptional.isPresent());
        assertThat(frameworkIDOptional.get(), is("1"));
        verify(registryCenter).getDirectly(HANode.FRAMEWORK_ID_NODE);
    }
    
    @Test
    public void assertSave() throws Exception {
        when(registryCenter.isExisted(HANode.FRAMEWORK_ID_NODE)).thenReturn(false);
        frameworkIDService.save("1");
        verify(registryCenter).isExisted(HANode.FRAMEWORK_ID_NODE);
        verify(registryCenter).persist(HANode.FRAMEWORK_ID_NODE, "1");
    }
}
