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

import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.config.json.JobConfigurationGsonFactory;
import org.junit.Test;

//CHECKSTYLE:OFF
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
//CHECKSTYLE:ON

/**
 * Test for dag config.
 **/
public class JobDagConfigTest {

    @Test
    public void assertBuildJobDagConfig() {
        JobDagConfig jobDagConfig = new JobDagConfig();
        jobDagConfig.setDagGroup("dagGroup");
        jobDagConfig.setDagDependencies("jobb,jobc");
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", JobType.SIMPLE, "0/1 * * * * ?", 3)
                .jobDagConfig(jobDagConfig)
                .build();
        assertNotNull(actual.getJobDagConfig());
        assertThat(actual.getJobDagConfig().getDagGroup(), is("dagGroup"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobDagConfigEmpty() {
        JobDagConfig jobDagConfig = new JobDagConfig();
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", JobType.SIMPLE, "0/1 * * * * ?", 3)
                .jobDagConfig(jobDagConfig)
                .build();
    }

    @Test
    public void assertJobDagConfigGson() {
        JobDagConfig jobDagConfig = new JobDagConfig();
        jobDagConfig.setDagGroup("dagGroup");
        jobDagConfig.setDagDependencies("jobb,jobc");
        jobDagConfig.setDagSkipWhenFail(true);
        jobDagConfig.setDagRunAlone(false);
        jobDagConfig.setRetryInterval(5000);
        jobDagConfig.setRetryTimes(5);
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", JobType.SIMPLE, "0/1 * * * * ?", 3)
                .jobDagConfig(jobDagConfig)
                .build();

        String s = JobConfigurationGsonFactory.toJson(actual);
        System.out.println(s);
        JobConfiguration fromJson = JobConfigurationGsonFactory.fromJson(s);
        System.out.println(fromJson.getJobDagConfig());
        assertNotNull(fromJson);
        assertNotNull(fromJson.getJobDagConfig());
        assertEquals(fromJson.getJobDagConfig().getDagGroup(), "dagGroup");
        assertEquals(fromJson.getJobDagConfig().getDagDependencies(), "jobb,jobc");
        assertEquals(fromJson.getJobDagConfig().getRetryTimes(), 5);
        assertEquals(fromJson.getJobDagConfig().getRetryInterval(), 5000);
        assertEquals(fromJson.getJobDagConfig().isDagRunAlone(), false);
        assertEquals(fromJson.getJobDagConfig().isDagSkipWhenFail(), true);
    }
}
