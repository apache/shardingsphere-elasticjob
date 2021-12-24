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
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.nio.charset.StandardCharsets;

/**
 * Job Listener.
 */
public abstract class AbstractJobListener implements CuratorCacheListener {
    
    @Override
    public final void event(final Type type, final ChildData oldData, final ChildData newData) {
        if (null == newData && null == oldData) {
            return;
        }
        String path = Type.NODE_DELETED == type ? oldData.getPath() : newData.getPath();
        byte[] data = Type.NODE_DELETED == type ? oldData.getData() : newData.getData();
        if (path.isEmpty()) {
            return;
        }
        dataChanged(path, type, null == data ? "" : new String(data, StandardCharsets.UTF_8));
    }
    
    protected abstract void dataChanged(String path, Type eventType, String data);
}
