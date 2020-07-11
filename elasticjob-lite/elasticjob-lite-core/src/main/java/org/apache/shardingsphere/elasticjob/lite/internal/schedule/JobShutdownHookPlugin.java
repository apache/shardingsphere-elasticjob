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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

/**
 * Job shutdown hook plugin.
 */
@Slf4j
@Getter
public final class JobShutdownHookPlugin implements SchedulerPlugin {
    
    private String jobName;
    
    @Setter
    private boolean cleanShutdown = true;
    
    @Override
    public void initialize(final String name, final Scheduler scheduler, final ClassLoadHelper classLoadHelper) throws SchedulerException {
        jobName = scheduler.getSchedulerName();
        registerShutdownHook();
    }
    
    /**
     * <p>
     * Called when the associated <code>Scheduler</code> is started, in order
     * to let the plug-in know it can now make calls into the scheduler if it
     * needs to.
     * </p>
     */
    @Override
    public void start() {

    }
    
    @Override
    public void shutdown() {
        CoordinatorRegistryCenter regCenter = JobRegistry.getInstance().getRegCenter(jobName);
        if (null == regCenter) {
            return;
        }
        LeaderService leaderService = new LeaderService(regCenter, jobName);
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        new InstanceService(regCenter, jobName).removeInstance();
    }
    
    private void registerShutdownHook() {
        log.info("Registering Quartz shutdown hook. {}", jobName);
        Thread t = new Thread("Quartz Shutdown-Hook " + jobName) {
            @Override
            public void run() {
                log.info("Shutting down Quartz... {}", jobName);
                JobScheduleController scheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
                if (null != scheduleController) {
                    scheduleController.shutdown(isCleanShutdown());
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(t);
    }
}
