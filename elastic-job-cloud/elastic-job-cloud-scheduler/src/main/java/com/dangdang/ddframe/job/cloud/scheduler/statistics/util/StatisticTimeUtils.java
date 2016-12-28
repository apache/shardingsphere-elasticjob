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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.util;

import java.util.Calendar;
import java.util.Date;

import com.dangdang.ddframe.job.statistics.StatisticInterval;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 统计时间工具类.
 *
 * @author liguangyun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticTimeUtils {
    
    /**
     * 获取以interval为时间间隔单位的统计时间.
     * 
     * @param interval 时间间隔
     * @return 时间对象
     */
    public static Date getCurrentStatisticTime(final StatisticInterval interval) {
        return getStatisticTime(interval, 0);
    }
    
    /**
     * 偏移offset个时间间隔单位，获取以interval为时间间隔单位的统计时间.
     * offset为负数表示时间向过去偏移，正数表示向未来偏移.
     * 
     * @param interval 时间间隔
     * @param offset 时间偏移量
     * @return 时间对象
     */
    public static Date getStatisticTime(final StatisticInterval interval, final int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        switch (interval) {
            case DAY:
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.add(Calendar.DATE, offset);
                break;
            case HOUR:
                calendar.set(Calendar.MINUTE, 0);
                calendar.add(Calendar.HOUR_OF_DAY, offset);
                break;
            case MINUTE:
            default:
                calendar.add(Calendar.MINUTE, offset);
                break;
        }
        return calendar.getTime();
    }
}
