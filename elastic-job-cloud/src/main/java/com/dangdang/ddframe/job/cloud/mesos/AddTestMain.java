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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.cloud.Internal.schedule.CloudTaskSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.Internal.config.CloudJobConfiguration;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;

/**
 * .
 *
 * @author zhangliang
 */
public class AddTestMain {
    
    public static void main(String[] args) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "elastic-job-cloud");
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
        regCenter.init();
        CloudTaskSchedulerRegistry.getInstance(regCenter).register(
                new CloudJobConfiguration("job_test", "0/5 * * * * ?", 5, 0.5d, 128, "docker", ""));
    }
}
