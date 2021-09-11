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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.shardingsphere.elasticjob.annotation.job.impl.SimpleTestJob;
import org.junit.Test;

public class ElasticJobConfigurationTest {
    
    @Test
    public void assertAnnotationJob() {
        ElasticJobConfiguration annotation = SimpleTestJob.class.getAnnotation(ElasticJobConfiguration.class);
        assertEquals(annotation.jobName(), "SimpleTestJob");
        assertEquals(annotation.cron(), "0/5 * * * * ?");
        assertEquals(annotation.shardingTotalCount(), 3);
        assertEquals(annotation.shardingItemParameters(), "0=Beijing,1=Shanghai,2=Guangzhou");
        assertArrayEquals(annotation.jobListenerTypes(), new String[] {"NOOP", "LOG"});
        Queue<String> propsKey = new LinkedList<>(Arrays.asList("print.title", "print.content"));
        Queue<String> propsValue = new LinkedList<>(Arrays.asList("test title", "test content"));
        for (ElasticJobProp prop :annotation.props()) {
            assertEquals(prop.key(), propsKey.poll());
            assertEquals(prop.value(), propsValue.poll());
        }
    }
}
