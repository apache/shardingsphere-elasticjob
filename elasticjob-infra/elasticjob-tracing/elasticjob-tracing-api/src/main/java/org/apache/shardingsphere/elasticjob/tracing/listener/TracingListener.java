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

package org.apache.shardingsphere.elasticjob.tracing.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;

/**
 * Tracing listener.
 */
public interface TracingListener {
    
    /**
     * Listen job execution event.
     *
     * @param jobExecutionEvent job execution event
     */
    @Subscribe
    @AllowConcurrentEvents
    void listen(JobExecutionEvent jobExecutionEvent);
    
    /**
     * Listen job status trace event.
     *
     * @param jobStatusTraceEvent job status trace event
     */
    @Subscribe
    @AllowConcurrentEvents
    void listen(JobStatusTraceEvent jobStatusTraceEvent);
}
