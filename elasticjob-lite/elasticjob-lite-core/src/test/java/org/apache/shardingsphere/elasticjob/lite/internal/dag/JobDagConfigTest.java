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

package org.apache.shardingsphere.elasticjob.lite.internal.dag;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobDagConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

//CHECKSTYLE:OFF
//CHECKSTYLE:ON

/**
 * Test for dag config.
 **/
public class JobDagConfigTest {

    @Test
    public void assertBuildJobDagConfig() {
        JobDagConfiguration jobDagConfig = new JobDagConfiguration();
        jobDagConfig.setDagName("dagGroup");
        jobDagConfig.setDagDependencies("jobb,jobc");
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", 3)
                .jobDagConfiguration(jobDagConfig)
                .cron("0/1 * * * * ?")
                .build();
        assertNotNull(actual.getJobDagConfiguration());
        assertThat(actual.getJobDagConfiguration().getDagName(), is("dagGroup"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobDagConfigEmpty() {
        JobDagConfiguration jobDagConfig = new JobDagConfiguration();
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", 3)
                .jobDagConfiguration(jobDagConfig)
                .build();
    }

    @Test
    public void assertJobDagConfigGson() {
        JobDagConfiguration jobDagConfig = new JobDagConfiguration();
        jobDagConfig.setDagName("dagGroup");
        jobDagConfig.setDagDependencies("jobb,jobc");
        jobDagConfig.setDagSkipWhenFail(true);
        jobDagConfig.setDagRunAlone(false);
        jobDagConfig.setRetryInterval(5000);
        jobDagConfig.setRetryTimes(5);
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", 3)
                .jobDagConfiguration(jobDagConfig)
                .cron("0/1 * * * * ?")
                .jobExecutorServiceHandlerType("simple")
                .description("test yaml")
                .failover(true)
                .jobParameter("a=1,b=-2")
                .overwrite(true)
                .build();

        System.out.println(actual);

        String marshal = YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(actual));
        System.out.println(marshal);

        JobConfiguration unmarshal = YamlEngine.unmarshal(marshal, JobConfigurationPOJO.class).toJobConfiguration();

        assertNotNull(unmarshal);
        assertNotNull(unmarshal.getJobDagConfiguration());
        assertEquals(unmarshal.getJobDagConfiguration().getDagName(), "dagGroup");
        assertEquals(unmarshal.getJobDagConfiguration().getDagDependencies(), "jobb,jobc");
        assertEquals(unmarshal.getJobDagConfiguration().getRetryTimes(), 5);
        assertEquals(unmarshal.getJobDagConfiguration().getRetryInterval(), 5000);
        assertEquals(unmarshal.getJobDagConfiguration().isDagRunAlone(), false);
        assertEquals(unmarshal.getJobDagConfiguration().isDagSkipWhenFail(), true);
    }
}
