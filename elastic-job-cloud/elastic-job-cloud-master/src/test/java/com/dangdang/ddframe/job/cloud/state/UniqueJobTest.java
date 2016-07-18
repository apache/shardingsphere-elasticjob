/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.state;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

public final class UniqueJobTest {
    
    @Test
    public void assertNew() {
        UniqueJob actual = new UniqueJob("test_job");
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getUniqueName(), startsWith("test_job@-@"));
    }
    
    @Test
    public void assertFrom() {
        UniqueJob actual = UniqueJob.from("test_job@-@0123456789");
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getUniqueName(), is("test_job@-@0123456789"));
    }
}
