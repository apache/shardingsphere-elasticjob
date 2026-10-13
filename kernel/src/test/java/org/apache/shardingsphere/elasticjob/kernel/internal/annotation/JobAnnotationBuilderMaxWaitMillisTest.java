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

package org.apache.shardingsphere.elasticjob.kernel.internal.annotation;

import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobAnnotationBuilderMaxWaitMillisTest {
    
    @Test
    void assertGenerateJobConfigurationWithDefaultMaxWaitMillis() {
        assertEquals(60_000L, JobAnnotationBuilder.generateJobConfiguration(DefaultJob.class).getMaxWaitMillis());
    }
    
    @Test
    void assertGenerateJobConfigurationWithCustomMaxWaitMillis() {
        assertEquals(45_000L, JobAnnotationBuilder.generateJobConfiguration(CustomJob.class).getMaxWaitMillis());
    }
    
    @ElasticJobConfiguration(jobName = "default_wait_job", shardingTotalCount = 1)
    private static final class DefaultJob {
    }
    
    @ElasticJobConfiguration(jobName = "custom_wait_job", shardingTotalCount = 1, maxWaitMillis = 45_000L)
    private static final class CustomJob {
    }
}
