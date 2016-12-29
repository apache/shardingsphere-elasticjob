/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class TaskResultMetaDataTest {
    
    private TaskResultMetaData metaData;
    
    @Before
    public void setUp() {
        metaData = new TaskResultMetaData();
    }
    
    @Test
    public void assertIncrementAndGet() {
        for (int i = 0; i < 100; i++) {
            assertThat(metaData.incrementAndGetSuccessCount(), is(i + 1));
            assertThat(metaData.incrementAndGetFailedCount(), is(i + 1));
            assertThat(metaData.getSuccessCount(), is(i + 1));
            assertThat(metaData.getFailedCount(), is(i + 1));
        }
    }
    
    @Test
    public void assertReset() {
        for (int i = 0; i < 100; i++) {
            metaData.incrementAndGetSuccessCount();
            metaData.incrementAndGetFailedCount();
        }
        metaData.reset();
        assertThat(metaData.getSuccessCount(), is(0));
        assertThat(metaData.getFailedCount(), is(0));
    }

}
