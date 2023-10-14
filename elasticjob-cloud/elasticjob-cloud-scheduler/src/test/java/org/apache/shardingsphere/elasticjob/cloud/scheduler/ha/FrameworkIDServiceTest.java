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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.ha;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrameworkIDServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    private FrameworkIDService frameworkIDService;
    
    @BeforeEach
    void init() {
        frameworkIDService = new FrameworkIDService(registryCenter);
    }
    
    @Test
    void assertFetch() {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("1");
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        assertTrue(frameworkIDOptional.isPresent());
        assertThat(frameworkIDOptional.get(), is("1"));
        verify(registryCenter).getDirectly(HANode.FRAMEWORK_ID_NODE);
    }
    
    @Test
    void assertSave() {
        when(registryCenter.isExisted(HANode.FRAMEWORK_ID_NODE)).thenReturn(false);
        frameworkIDService.save("1");
        verify(registryCenter).isExisted(HANode.FRAMEWORK_ID_NODE);
        verify(registryCenter).persist(HANode.FRAMEWORK_ID_NODE, "1");
    }
}
