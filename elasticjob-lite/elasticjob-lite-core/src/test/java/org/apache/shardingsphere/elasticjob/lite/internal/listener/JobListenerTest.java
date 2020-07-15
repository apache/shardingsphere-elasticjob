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

package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.fixture.FooJobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobListenerTest {
    
    @Mock
    private ChildData childData;
    
    @Mock
    private List list;
    
    private FooJobListener fooJobListener;
    
    @Before
    public void setUp() {
        fooJobListener = new FooJobListener(list);
    }
    
    @Test
    public void assertChildEventWhenEventDataIsEmpty() {
        when(childData.getPath()).thenReturn("");
        fooJobListener.event(null, null, childData);
        verify(list, times(0)).clear();
    }
    
    @Test
    public void assertChildEventSuccess() {
        when(childData.getPath()).thenReturn("/test");
        when(childData.getData()).thenReturn("test".getBytes());
        fooJobListener.event(null, null, childData);
        verify(list).clear();
    }
}
