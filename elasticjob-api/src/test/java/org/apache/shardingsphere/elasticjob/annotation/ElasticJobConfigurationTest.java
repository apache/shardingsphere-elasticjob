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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.shardingsphere.elasticjob.annotation.job.impl.SimpleTestJob;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfigurationFactory;
import org.junit.Test;

public final class ElasticJobConfigurationTest {
    
    @Test
    public void assertAnnotationJob() {
        ElasticJobConfiguration annotation = SimpleTestJob.class.getAnnotation(ElasticJobConfiguration.class);
        assertThat(annotation.jobName(), is("SimpleTestJob"));
        assertThat(annotation.cron(), is("0/5 * * * * ?"));
        assertThat(annotation.shardingTotalCount(), is(3));
        assertThat(annotation.shardingItemParameters(), is("0=Beijing,1=Shanghai,2=Guangzhou"));
        for (Class<? extends JobExtraConfigurationFactory> factory :annotation.extraConfigurations()) {
            assertThat(factory, is(SimpleTracingConfigurationFactory.class));
        }
        assertArrayEquals(annotation.jobListenerTypes(), new String[] {"NOOP", "LOG"});
        Queue<String> propsKey = new LinkedList<>(Arrays.asList("print.title", "print.content"));
        Queue<String> propsValue = new LinkedList<>(Arrays.asList("test title", "test content"));
        for (ElasticJobProp prop :annotation.props()) {
            assertThat(prop.key(), is(propsKey.poll()));
            assertThat(prop.value(), is(propsValue.poll()));
        }
    }
}
