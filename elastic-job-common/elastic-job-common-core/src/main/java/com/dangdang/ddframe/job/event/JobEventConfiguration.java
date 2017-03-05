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

package com.dangdang.ddframe.job.event;

/**
 * 作业事件配置标识接口.
 *
 * @author caohao
 */
public interface JobEventConfiguration extends JobEventIdentity {
    
    /**
     * 创建作业事件监听器.
     * 
     * @return 作业事件监听器.
     * @throws JobEventListenerConfigurationException 作业事件监听器配置异常
     */
    JobEventListener createJobEventListener() throws JobEventListenerConfigurationException;
}
