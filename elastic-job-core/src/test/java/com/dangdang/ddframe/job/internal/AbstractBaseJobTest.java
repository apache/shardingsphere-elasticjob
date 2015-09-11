/**
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

package com.dangdang.ddframe.job.internal;

import org.junit.After;
import org.junit.Before;

import com.dangdang.ddframe.job.api.AbstractElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.test.NestedZookeeperServers;

public abstract class AbstractBaseJobTest {
    
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedZookeeperServers.ZK_CONNECTION_STRING, "zkRegTestCenter", 2000, 1000 * 60, 3);
    
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    private final JobConfiguration jobConfiguration = createJobConfiguration();
    
    @Before
    public final void initRegistryCenter() throws Exception {
        NestedZookeeperServers.getInstance().startServerIfNotStarted();
        regCenter.init();
        regCenter.persist("/testJob", "");
    }
    
    private JobConfiguration createJobConfiguration() {
        JobConfiguration result = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
        result.setShardingItemParameters("0=A,1=B,2=C");
        result.setJobParameter("para");
        return result;
    }
    
    @After
    public final void closeRegistryCenter() {
        regCenter.remove("/testJob");
        regCenter.close();
    }
    
    protected final CoordinatorRegistryCenter getRegistryCenter() {
        return regCenter;
    }
    
    protected final JobConfiguration getJobConfig() {
        return jobConfiguration;
    }
    
    public static class TestJob extends AbstractElasticJob {
        
        @Override
        protected void executeJob(final JobExecutionMultipleShardingContext jobExecutionShardingContext) {
        }
    }
}
