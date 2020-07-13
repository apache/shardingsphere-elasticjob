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

package org.apache.shardingsphere.elasticjob.lite.dag;

import org.apache.commons.lang3.RandomUtils;
import org.apache.shardingsphere.elasticjob.lite.api.JobScheduler;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.simple.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Test dag job register.
 **/
public class DagJobRegisterTest extends BaseRegCenterLocal {

    @Test
    public void test4CurrentZkData() {
        JobNodeStorage jobNodeStorage = new JobNodeStorage(zkRegCenter, "jobA");
        String originalJobClassName = jobNodeStorage.getJobRootNodeData();
        System.out.println(originalJobClassName);
        System.out.println(jobNodeStorage.isJobRootNodeExisted());
    }

    @Test
    public void test4DagRegister() throws InterruptedException {
        JobConfiguration jobConfiguration = simpleJobConfiguration("jobA", "1/10 * * * * ?", 3, "group1", "self");
        JobScheduler jobScheduler = new JobScheduler(zkRegCenter, new TestSimpleJob(), jobConfiguration);
        System.out.println("==============");
        jobScheduler.init();

        System.out.println("===============");
        JobNodeStorage jobNodeStorage = new JobNodeStorage(zkRegCenter, "jobA");
        String jobAConfigString = jobNodeStorage.getJobNodeData("config");
        System.out.println(jobAConfigString);

        DagNodeStorage dagNodeStorage = new DagNodeStorage(zkRegCenter, "group1", "jobA");
        Map<String, Set<String>> allDagConfigJobs = dagNodeStorage.getAllDagConfigJobs();
        allDagConfigJobs.forEach((key, value) -> System.out.println(key + "=" + value));

        TimeUnit.SECONDS.sleep(500L);
    }

    @Test
    public void test4ConfigurationService() {
        JobConfiguration jobConfiguration = simpleJobConfiguration("jobA", "1/10 * * * * ? 2099", 3, "group1", "jobB,jobC");
        ConfigurationService configurationService = new ConfigurationService(zkRegCenter, "jobA");
        configurationService.persist(TestSimpleJob.class.getCanonicalName(), jobConfiguration);
    }

    private JobConfiguration simpleJobConfiguration(final String jobName, final String cron, final int shardingTotalCount, final String groupName, final String depens) {
        JobConfiguration jobConfiguration = JobConfiguration.newBuilder(jobName, JobType.SIMPLE, cron, shardingTotalCount)
                .description("Foo simple job")
                .misfire(true)
                .overwrite(true)
                .shardingItemParameters("0=a,1=B,2=C")
                .setProperty("name", "1234")
                .setProperty("sec", "ces")
                .jobDagConfig(new JobDagConfig(groupName, depens, 3, 3000, false, false))
                .build();
        return jobConfiguration;
    }

    public static class TestSimpleJob implements SimpleJob {

        @Override
        public void execute(final ShardingContext shardingContext) {
            System.out.println("Simple Job execute....." + shardingContext.toString());
            try {
                TimeUnit.SECONDS.sleep(RandomUtils.nextLong(1L, 3L));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Simple Job execute END!");
        }
    }
}
