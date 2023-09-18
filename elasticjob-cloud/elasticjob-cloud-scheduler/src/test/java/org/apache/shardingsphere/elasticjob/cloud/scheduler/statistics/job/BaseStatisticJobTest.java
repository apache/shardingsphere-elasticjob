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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BaseStatisticJobTest {
    
    private TestStatisticJob testStatisticJob;
    
    @BeforeEach
    public void setUp() {
        testStatisticJob = new TestStatisticJob();
    }
    
    @Test
    public void assertGetTriggerName() {
        assertThat(testStatisticJob.getTriggerName(), is(TestStatisticJob.class.getSimpleName() + "Trigger"));
    }
    
    @Test
    public void assertGetJobName() {
        assertThat(testStatisticJob.getJobName(), is(TestStatisticJob.class.getSimpleName()));
    }
    
    @Test
    public void assertFindBlankStatisticTimes() {
        for (StatisticInterval each : StatisticInterval.values()) {
            int num = -2;
            for (Date eachTime : testStatisticJob.findBlankStatisticTimes(StatisticTimeUtils.getStatisticTime(each, num - 1), each)) {
                assertThat(eachTime.getTime(), is(StatisticTimeUtils.getStatisticTime(each, num++).getTime()));
            }
        }
    }

}
