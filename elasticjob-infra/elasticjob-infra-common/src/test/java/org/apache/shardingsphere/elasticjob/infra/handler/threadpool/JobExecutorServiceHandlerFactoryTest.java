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

package org.apache.shardingsphere.elasticjob.infra.handler.threadpool;

import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.impl.CPUUsageJobExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.impl.SingleThreadJobExecutorServiceHandler;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobExecutorServiceHandlerFactoryTest {
    
    @Test
    public void assertGetDefaultHandler() {
        assertThat(JobExecutorServiceHandlerFactory.getHandler(""), instanceOf(CPUUsageJobExecutorServiceHandler.class));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetInvalidHandler() {
        JobExecutorServiceHandlerFactory.getHandler("INVALID");
    }
    
    @Test
    public void assertGetHandler() {
        assertThat(JobExecutorServiceHandlerFactory.getHandler("SINGLE_THREAD"), instanceOf(SingleThreadJobExecutorServiceHandler.class));
    }
}
