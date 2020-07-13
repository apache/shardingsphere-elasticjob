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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.RandomUtils;
import org.apache.shardingsphere.elasticjob.lite.api.JobScheduler;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.simple.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Dag job test.
 **/
public class DagJobTest extends BaseRegCenterLocal {

    /**
     * dag struct.
     * DAG jobs :
     *      JobROOT
     *         /   \
     *       JobA   \
     *       /       \
     *      JobB      \
     *         \     /
     *           JobC
     */
    @Test
    public void rootJob() throws InterruptedException {
        JobConfiguration jobConfiguration = simpleJobConfiguration("JobROOT", "1/59 * * * * ?", 2, "DAGX", "self", "0=A,1=B");
        JobScheduler jobScheduler = new JobScheduler(zkRegCenter, new TestSimpleJob(), jobConfiguration, genTracingConfiguration());
        jobScheduler.init();
        System.out.println("JOBROOT init complete!");

        TimeUnit.SECONDS.sleep(5000L);
    }

    @Test
    public void testJobA() throws InterruptedException {
        JobConfiguration jobConfiguration = simpleJobConfiguration("JobA", "1/59 * * * * ? 2099", 1, "DAGX", "JobROOT", "0=A,1=B");
        JobScheduler jobScheduler = new JobScheduler(zkRegCenter, new TestSimpleJob(), jobConfiguration, genTracingConfiguration());
        jobScheduler.init();
        System.out.println("JobA init complete!");

        TimeUnit.SECONDS.sleep(5000L);
    }

    @Test
    public void testJobB() throws InterruptedException {
        JobConfiguration jobConfiguration = simpleJobConfiguration("JobB", "1/59 * * * * ? 2099", 3, "DAGX", "JobA", "0=A,1=B,2=C");
        JobScheduler jobScheduler = new JobScheduler(zkRegCenter, new TestSimpleJob(), jobConfiguration, genTracingConfiguration());
        jobScheduler.init();
        System.out.println("JobB init complete!");

        TimeUnit.SECONDS.sleep(5000L);
    }

    @Test
    public void testJobC() throws InterruptedException {
        JobConfiguration jobConfiguration = simpleJobConfiguration("JobC", "1/59 * * * * ? 2099", 3, "DAGX", "JobB,JobROOT", "0=A,1=B,2=C");
        JobScheduler jobScheduler = new JobScheduler(zkRegCenter, new TestSimpleJob(), jobConfiguration, genTracingConfiguration());
        jobScheduler.init();
        System.out.println("JobC init complete!");

        TimeUnit.SECONDS.sleep(5000L);
    }

    private TracingConfiguration genTracingConfiguration() {
        return new TracingConfiguration("RDB", getDataSource());
    }

    private DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        dataSource.setUrl("jdbc:mysql://localhost:6216/dag?useUnicode=true&characterEncoding=utf8");
        dataSource.setUsername("dag");
        dataSource.setPassword("dag");
        return dataSource;
    }

    private JobConfiguration simpleJobConfiguration(final String jobName, final String cron, final int shardingTotalCount,
                                                    final String groupName, final String depens, final String shardingParameters) {
        JobConfiguration jobConfiguration = JobConfiguration.newBuilder(jobName, JobType.SIMPLE, cron, shardingTotalCount)
                .description("Foo simple job " + jobName)
                .overwrite(true)
                .shardingItemParameters(shardingParameters)
                .setProperty("name", "1234")
                .setProperty("sec", "ces")
                .jobDagConfig(new JobDagConfig(groupName, depens, 3, 10, false, false))
                .build();
        return jobConfiguration;
    }

    public static class TestSimpleJob implements SimpleJob {

        @Override
        public void execute(final ShardingContext shardingContext) {
            System.out.println("Simple Job execute....." + shardingContext.toString());
            try {
                TimeUnit.SECONDS.sleep(RandomUtils.nextLong(5L, 10L));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (RandomUtils.nextInt(1, 100) % 4 == 0) {
                throw new RuntimeException("Job Error!");
            }
            System.out.println("Simple Job execute END!");
        }
    }
}
