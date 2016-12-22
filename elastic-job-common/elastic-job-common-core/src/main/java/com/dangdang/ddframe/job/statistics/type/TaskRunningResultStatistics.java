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

package com.dangdang.ddframe.job.statistics.type;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 任务运行结果统计数据.
 *
 * @author liguangyun
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString(of = {"successCount", "failedCount", "statisticUnit", "statisticsTime"})
public class TaskRunningResultStatistics {
    
    private long id;
    
    private final int successCount;
    
    private final int failedCount;
    
    private final StatisticUnit statisticUnit;
    
    private final Date statisticsTime;

    private Date creationTime;
    
    /**
     * 统计单位.
     *
     * @author liguangyun
     */
    public enum StatisticUnit {
        
        MINUTE, HOUR, DAY
    }
}
