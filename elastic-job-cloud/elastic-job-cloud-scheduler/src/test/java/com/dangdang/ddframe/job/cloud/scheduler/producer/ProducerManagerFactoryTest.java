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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ProducerManagerFactoryTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    @After
    public void clear() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(ProducerManagerFactory.class, ProducerManagerFactory.class.getDeclaredField("instance"), null);
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(ProducerManagerFactory.getInstance(regCenter), is(ProducerManagerFactory.getInstance(regCenter)));
    }
}
