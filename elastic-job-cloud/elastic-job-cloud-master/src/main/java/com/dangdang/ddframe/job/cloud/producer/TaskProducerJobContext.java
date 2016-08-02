/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.producer;

import org.quartz.JobKey;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 发布任务的作业上下文.
 *
 * @author caohao
 */
class TaskProducerJobContext {
    
    private static final TaskProducerJobContext INSTANCE = new TaskProducerJobContext();
    
    private final ConcurrentHashMap<JobKey, List<String>> cronTasks = new ConcurrentHashMap<>(256, 1);
    
    public static TaskProducerJobContext getInstance() {
        return INSTANCE;
    }
    
    //TODO 并发优化
    synchronized void put(final JobKey jobKey, final String jobName) {
        remove(jobName);
        if (cronTasks.containsKey(jobKey)) {
            List<String> taskList = cronTasks.get(jobKey);
            if (!taskList.contains(jobName)) {
                taskList.add(jobName);
            }
        } else {
            List<String> taskList = new CopyOnWriteArrayList<>();
            taskList.add(jobName);
            cronTasks.put(jobKey, taskList);
        }
    }
    
    synchronized void remove(final String jobName) {
        for (Entry<JobKey, List<String>> each : cronTasks.entrySet()) {
            JobKey jobKey = each.getKey();
            List<String> jobNames = each.getValue();
            jobNames.remove(jobName);
            if (jobNames.isEmpty()) {
                cronTasks.remove(jobKey);
            }
        }
    }
    
    List<String> get(final JobKey jobKey) {
        return cronTasks.get(jobKey);
    }
    
    synchronized boolean contains(final JobKey jobKey) {
        return cronTasks.contains(jobKey);
    }
    
    synchronized void clear() {
        cronTasks.clear();
    }
}
