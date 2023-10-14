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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobKey;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TransientProducerRepositoryTest {
    
    private final JobKey jobKey = JobKey.jobKey("0/45 * * * * ?");
    
    private final String jobName = "test_job";
    
    private final TransientProducerRepository transientProducerRepository = new TransientProducerRepository();
    
    @Test
    void assertPutJobKey() {
        transientProducerRepository.put(jobKey, jobName);
        assertThat(transientProducerRepository.get(jobKey).get(0), is(jobName));
        transientProducerRepository.remove(jobName);
    }
    
    @Test
    void assertPutJobWithChangedCron() {
        transientProducerRepository.put(jobKey, jobName);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        transientProducerRepository.put(newJobKey, jobName);
        assertTrue(transientProducerRepository.get(jobKey).isEmpty());
        assertThat(transientProducerRepository.get(newJobKey).get(0), is(jobName));
        transientProducerRepository.remove(jobName);
    }
    
    @Test
    void assertPutMoreJobWithChangedCron() {
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
    void assertRemoveJobKey() {
        transientProducerRepository.put(jobKey, jobName);
        transientProducerRepository.remove(jobName);
        assertTrue(transientProducerRepository.get(jobKey).isEmpty());
    }
    
    @Test
    void assertContainsKey() {
        transientProducerRepository.put(jobKey, jobName);
        assertTrue(transientProducerRepository.containsKey(jobKey));
        transientProducerRepository.remove(jobName);
        assertFalse(transientProducerRepository.containsKey(jobKey));
    }
}
