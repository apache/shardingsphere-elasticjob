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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class TransientProducerRepositoryTest {
    
    private final JobKey jobKey = JobKey.jobKey("0/45 * * * * ?");
    
    private final String jobName = "test_job";
    
    private final TransientProducerRepository transientProducerRepository = new TransientProducerRepository();
    
    @Test
    public void assertPutJobKey() {
        transientProducerRepository.put(jobKey, jobName);
        assertThat(transientProducerRepository.get(jobKey).get(0), is(jobName));
        transientProducerRepository.remove(jobName);
    }
    
    @Test
    public void assertPutJobWithChangedCron() {
        transientProducerRepository.put(jobKey, jobName);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        transientProducerRepository.put(newJobKey, jobName);
        assertTrue(transientProducerRepository.get(jobKey).isEmpty());
        assertThat(transientProducerRepository.get(newJobKey).get(0), is(jobName));
        transientProducerRepository.remove(jobName);
    }
    
    @Test
    public void assertPutMoreJobWithChangedCron() {
        String jobName2 = "other_test_job";
        transientProducerRepository.put(jobKey, jobName);
        transientProducerRepository.put(jobKey, jobName2);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        transientProducerRepository.put(newJobKey, jobName);
        assertThat(transientProducerRepository.get(jobKey).get(0), is(jobName2));
        assertThat(transientProducerRepository.get(newJobKey).get(0), is(jobName));
        transientProducerRepository.remove(jobName);
        transientProducerRepository.remove(jobName2);
    }
    
    @Test
    public void assertRemoveJobKey() {
        transientProducerRepository.put(jobKey, jobName);
        transientProducerRepository.remove(jobName);
        assertTrue(transientProducerRepository.get(jobKey).isEmpty());
    }
    
    @Test
    public void assertContainsKey() {
        transientProducerRepository.put(jobKey, jobName);
        assertTrue(transientProducerRepository.containsKey(jobKey));
        transientProducerRepository.remove(jobName);
        assertFalse(transientProducerRepository.containsKey(jobKey));
    }
}
