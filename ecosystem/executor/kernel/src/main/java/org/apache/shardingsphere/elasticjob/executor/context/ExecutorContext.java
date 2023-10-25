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

package org.apache.shardingsphere.elasticjob.executor.context;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.Reloadable;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.IOException;
import java.util.Properties;

/**
 * Executor context.
 */
public final class ExecutorContext {
    
    public ExecutorContext(final JobConfiguration jobConfig) {
        for (Reloadable<?> each : ShardingSphereServiceLoader.getServiceInstances(Reloadable.class)) {
            each.init(jobConfig);
        }
    }
    
    /**
     * Reload all reloadable item if necessary.
     *
     * @param jobConfig job configuration
     */
    public void reloadIfNecessary(final JobConfiguration jobConfig) {
        ShardingSphereServiceLoader.getServiceInstances(Reloadable.class).forEach(each -> each.reloadIfNecessary(jobConfig));
    }
    
    /**
     * Get instance.
     *
     * @param targetClass target class
     * @param <T> target type
     * @return instance
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> targetClass) {
        return (T) TypedSPILoader.getService(Reloadable.class, targetClass, new Properties()).getInstance();
    }
    
    /**
     * Shutdown all closeable instances.
     */
    public void shutdown() {
        for (Reloadable<?> each : ShardingSphereServiceLoader.getServiceInstances(Reloadable.class)) {
            try {
                each.close();
            } catch (final IOException ignored) {
            }
        }
    }
}
