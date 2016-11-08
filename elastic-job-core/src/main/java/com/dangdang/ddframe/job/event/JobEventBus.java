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

package com.dangdang.ddframe.job.event;

import com.dangdang.ddframe.job.util.concurrent.ExecutorServiceObject;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 运行痕迹事件总线.
 * 
 * @author zhangliang
 * @author caohao
 */
public class JobEventBus {
    
    private final Collection<JobEventConfiguration> jobEventConfigs;
    
    @Getter
    private final ExecutorServiceObject executorServiceObject;
    
    private final EventBus eventBus;
    
    public JobEventBus(final JobEventConfiguration... jobEventConfigs) {
        this.jobEventConfigs = getJobEventConfiguration(jobEventConfigs);
        executorServiceObject = new ExecutorServiceObject("job-event", Runtime.getRuntime().availableProcessors() * 2);
        eventBus = new AsyncEventBus(executorServiceObject.createExecutorService());
        register();
    }
    
    private Collection<JobEventConfiguration> getJobEventConfiguration(final JobEventConfiguration... jobEventConfigs) {
        Map<String, JobEventConfiguration> result = new HashMap<>(jobEventConfigs.length, 1);
        for (JobEventConfiguration each : jobEventConfigs) {
            result.put(each.getIdentity(), each);
        }
        return result.values();
    }
    
    private void register() {
        for (JobEventConfiguration each : jobEventConfigs) {
            if (null != each) {
                eventBus.register(each.createJobEventListener());
            }
        }
    }
    
    /**
     * 发布事件.
     *
     * @param event 作业事件
     */
    public void post(final JobEvent event) {
        if (!jobEventConfigs.isEmpty() && !executorServiceObject.isShutdown()) {
            eventBus.post(event);
        }
    }
}
