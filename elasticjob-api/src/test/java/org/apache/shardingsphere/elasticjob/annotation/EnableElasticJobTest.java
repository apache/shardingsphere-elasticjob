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

package org.apache.shardingsphere.elasticjob.annotation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.shardingsphere.elasticjob.annotation.job.impl.SimpleTestJob;
import org.junit.Test;

public class EnableElasticJobTest {
    
    @Test
    public void assertAnnotationJob() {
        EnableElasticJob annotation = SimpleTestJob.class.getAnnotation(EnableElasticJob.class);
        assertEquals(annotation.jobName(), "SimpleTestJob");
        assertEquals(annotation.cron(), "0/5 * * * * ?");
        assertEquals(annotation.shardingTotalCount(), 3);
        assertEquals(annotation.shardingItemParameters(), "0=Beijing,1=Shanghai,2=Guangzhou");
        assertArrayEquals(annotation.jobListenerTypes(), new String[] {"NOOP", "LOG"});
        for (ElasticJobProp prop :annotation.props()) {
            assertEquals(prop.key(), "print.title");
            assertEquals(prop.value(), "test title");
        }
    }
}
