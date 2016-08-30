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

package com.dangdang.ddframe.job.lite.internal.listener;

import com.dangdang.ddframe.job.lite.internal.listener.fixture.FooJobListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobListenerTest {
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private TreeCacheEvent event;
    
    private FooJobListener fooJobListener = new FooJobListener();
    
    @Test
    public void assertChildEventWhenEventDataIsEmpty() throws Exception {
        when(event.getData()).thenReturn(null);
        fooJobListener.childEvent(client, event);
        verify(client, times(0)).getNamespace();
    }
    
    @Test
    public void assertChildEventSuccess() throws Exception {
        when(event.getData()).thenReturn(new ChildData("/test_job", null, null));
        fooJobListener.childEvent(client, event);
        verify(client).getNamespace();
    }
}
