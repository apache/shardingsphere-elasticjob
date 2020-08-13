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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DagJobRetryTriggerTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    private JobRetryTrigger jobRetryTrigger;

    @Before
    public void setUp() {
        jobRetryTrigger = new JobRetryTrigger(regCenter, "testDag");
        ReflectionUtils.setFieldValue(jobRetryTrigger, "regCenter", regCenter);
    }

    @Test
    public void consumeMessage() throws Exception {
        when(regCenter.isExisted("/dag/testDag/states")).thenReturn(true);
        when(regCenter.getDirectly("/dag/testDag/states")).thenReturn("running");
        jobRetryTrigger.consumeMessage("testDag||job1");
    }

    @Test
    public void stateChanged() {
        jobRetryTrigger.stateChanged((CuratorFramework) regCenter.getRawClient(), ConnectionState.CONNECTED);
    }
}
