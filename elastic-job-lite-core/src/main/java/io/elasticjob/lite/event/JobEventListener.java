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

package io.elasticjob.lite.event;


import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.elasticjob.lite.event.type.JobExecutionEvent;
import io.elasticjob.lite.event.type.JobStatusTraceEvent;

/**
 * 作业事件监听器.
 *
 * @author zhangliang
 */
public interface JobEventListener extends JobEventIdentity {
    
    /**
     * 作业执行事件监听执行.
     *
     * @param jobExecutionEvent 作业执行事件
     */
    @Subscribe
    @AllowConcurrentEvents
    void listen(JobExecutionEvent jobExecutionEvent);
    
    /**
     * 作业状态痕迹事件监听执行.
     *
     * @param jobStatusTraceEvent 作业状态痕迹事件
     */
    @Subscribe
    @AllowConcurrentEvents
    void listen(JobStatusTraceEvent jobStatusTraceEvent);
}
