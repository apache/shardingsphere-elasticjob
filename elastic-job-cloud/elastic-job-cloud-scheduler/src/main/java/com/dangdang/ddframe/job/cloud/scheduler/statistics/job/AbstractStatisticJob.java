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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.statistics.StatisticInterval;

/**
 * 统计作业抽象类.
 *
 * @author liguangyun
 */
abstract class AbstractStatisticJob implements StatisticJob {
    
    String getJobName() {
        return this.getClass().getSimpleName();
    }
    
    String getTriggerName() {
        return this.getClass().getSimpleName() + "Trigger";
    }
    
    List<Date> findBlankStatisticTimes(final Date latestStatisticTime, final StatisticInterval statisticInterval) {
        List<Date> result = new ArrayList<>();
        int previousInterval = -1;
        Date previousTime = StatisticTimeUtils.getStatisticTime(statisticInterval, previousInterval);
        while (previousTime.after(latestStatisticTime)) {
            result.add(previousTime);
            previousTime = StatisticTimeUtils.getStatisticTime(statisticInterval, --previousInterval);
        }
        Collections.sort(result);
        return result;
    }
}
