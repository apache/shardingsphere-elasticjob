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

package com.dangdang.ddframe.job.executor.handler.impl;

import com.dangdang.ddframe.job.executor.handler.ExecutorServiceHandler;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认线程池服务处理器.
 * 
 * @author zhangliang
 */
public final class DefaultExecutorServiceHandler implements ExecutorServiceHandler {
    
    @Override
    public ExecutorService createExecutorService() {
        int threadSize = Runtime.getRuntime().availableProcessors() * 2;
        return MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(threadSize, threadSize, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>())));
    }
}
