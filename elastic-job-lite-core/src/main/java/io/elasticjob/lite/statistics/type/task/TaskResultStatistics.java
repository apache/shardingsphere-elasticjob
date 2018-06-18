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

package io.elasticjob.lite.statistics.type.task;

import io.elasticjob.lite.statistics.StatisticInterval;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 任务运行结果统计数据.
 *
 * @author liguangyun
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public final class TaskResultStatistics {
    
    private long id;
    
    private final int successCount;
    
    private final int failedCount;
    
    private final StatisticInterval statisticInterval;
    
    private final Date statisticsTime;
    
    private Date creationTime = new Date();
}
