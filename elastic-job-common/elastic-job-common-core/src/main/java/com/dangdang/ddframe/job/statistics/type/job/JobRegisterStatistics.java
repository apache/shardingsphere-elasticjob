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

package com.dangdang.ddframe.job.statistics.type.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 作业注册到Cloud平台统计数据.
 *
 * @author liguangyun
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public final class JobRegisterStatistics {
    
    private long id;
    
    private final int registeredCount;
    
    private final Date statisticsTime;
    
    private Date creationTime = new Date();
}
