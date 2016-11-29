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

package com.dangdang.ddframe.job.executor.handler;

import java.util.concurrent.ExecutorService;

/**
 * 线程池服务处理器.
 * 
 * <p>用于作业内部的线程池处理数据使用. 目前仅用于数据流类型.</p>
 *
 * @author zhangliang
 */
public interface ExecutorServiceHandler {
    
    /**
     * 创建线程池服务对象.
     * 
     * @param jobName 作业名
     * 
     * @return 线程池服务对象
     */
    ExecutorService createExecutorService(final String jobName);
}
