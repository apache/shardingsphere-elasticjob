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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.Interval;

public class StatisticTimeUtilsTest {
    
    @Test
    public void assertGetCurrentStatisticTime() {
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(Interval.MINUTE), Interval.MINUTE), is(getTimeStr(getNow(), Interval.MINUTE)));
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(Interval.HOUR), Interval.HOUR), is(getTimeStr(getNow(), Interval.HOUR)));
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(Interval.DAY), Interval.DAY), is(getTimeStr(getNow(), Interval.DAY)));
    }
    
    @Test
    public void assertGetStatisticTime() {
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(Interval.MINUTE, -1), Interval.MINUTE), is(getTimeStr(getLastMinute(), Interval.MINUTE)));
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(Interval.HOUR, -1), Interval.HOUR), is(getTimeStr(getLastHour(), Interval.HOUR)));
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(Interval.DAY, -1), Interval.DAY), is(getTimeStr(getYesterday(), Interval.DAY)));
    }
    
    private Date getNow() {
        return new Date();
    }
    
    private Date getLastMinute() {
        return new Date(getNow().getTime() - 60 * 1000);
    }
    
    private Date getLastHour() {
        return new Date(getNow().getTime() - 60 * 60 * 1000);
    }
    
    private Date getYesterday() {
        return new Date(getNow().getTime() - 24 * 60 * 60 * 1000);
    }
    
    private String getTimeStr(final Date time, final Interval interval) {
        switch (interval) {
            case DAY:
                return new SimpleDateFormat("yyyy-MM-dd").format(time) + " :00:00:00";
            case HOUR:
                return new SimpleDateFormat("yyyy-MM-dd HH").format(time) + ":00:00";
            case MINUTE:
            default:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time) + ":00";
        }
    }
}
