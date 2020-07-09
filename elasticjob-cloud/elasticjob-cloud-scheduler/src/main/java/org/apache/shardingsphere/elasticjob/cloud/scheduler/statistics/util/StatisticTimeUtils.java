/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Statistic time utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticTimeUtils {
    
    /**
     * Get the statistical time with the interval unit.
     *
     * @param interval interval
     * @return Date
     */
    public static Date getCurrentStatisticTime(final StatisticInterval interval) {
        return getStatisticTime(interval, 0);
    }
    
    /**
     * Get the statistical time with the interval unit.
     *
     * @param interval interval
     * @param offset offset
     * @return Date
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
