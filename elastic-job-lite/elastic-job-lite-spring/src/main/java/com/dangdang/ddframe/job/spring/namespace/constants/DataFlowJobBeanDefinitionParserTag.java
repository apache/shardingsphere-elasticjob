/*
 * Copyright 1999_2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE_2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.spring.namespace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 数据流作业属性解析标签.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataFlowJobBeanDefinitionParserTag {
    
    public static final String PROCESS_COUNT_INTERVAL_SECONDS_ATTRIBUTE = "process-count-interval-seconds";
    
    public static final String CONCURRENT_DATA_PROCESS_THREAD_COUNT_ATTRIBUTE = "concurrent-data-process-thread-count";
    
    public static final String FETCH_DATA_COUNT_ATTRIBUTE = "fetch-data-count";
    
    public static final String STREAMING_PROCESS_ATTRIBUTE = "streaming-process";
}
