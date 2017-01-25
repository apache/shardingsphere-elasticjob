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
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperElectionService;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HAServiceTest {
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Mock
    private ZookeeperElectionService electionService;
    
    private HAService haService;
    
    @Before
    public void init() throws NoSuchFieldException {
        when(registryCenter.getRawClient()).thenReturn(client);
        haService = new HAService(registryCenter);
        ReflectionUtils.setFieldValue(haService, "electionService", electionService);
    }
    
    @Test
    public void assertStart() {
        haService.start();
        verify(electionService).startLeadership();
    }
    
    @Test
    public void assertStop() {
        haService.stop();
        verify(electionService).close();
    }
}
