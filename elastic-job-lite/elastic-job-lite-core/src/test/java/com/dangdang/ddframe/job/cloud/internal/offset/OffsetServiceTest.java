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

package com.dangdang.ddframe.job.cloud.internal.offset;

import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.cloud.fixture.TestJob;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public final class OffsetServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private final JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private OffsetService offsetService = new OffsetService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(offsetService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertUpdateOffset() {
        offsetService.updateOffset(0, "offset0");
        verify(jobNodeStorage).createJobNodeIfNeeded("offset/0");
        verify(jobNodeStorage).updateJobNode("offset/0", "offset0");
    }
    
    @Test
    public void assertGetOffsets() {
        when(jobNodeStorage.getJobNodeDataDirectly("offset/0")).thenReturn("offset0");
        when(jobNodeStorage.getJobNodeDataDirectly("offset/1")).thenReturn("");
        when(jobNodeStorage.getJobNodeDataDirectly("offset/2")).thenReturn("offset2");
        Map<Integer, String> expected = new HashMap<>(1);
        expected.put(0, "offset0");
        assertThat(offsetService.getOffsets(Arrays.asList(0, 1)), is(expected));
        verify(jobNodeStorage).getJobNodeDataDirectly("offset/0");
        verify(jobNodeStorage).getJobNodeDataDirectly("offset/1");
    }
}
