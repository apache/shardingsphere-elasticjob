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

import com.dangdang.ddframe.job.statistics.StatisticInterval;

public class StatisticTimeUtilsTest {
    
    @Test
    public void assertGetCurrentStatisticTime() {
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(StatisticInterval.MINUTE), StatisticInterval.MINUTE), is(getTimeStr(getNow(), StatisticInterval.MINUTE)));
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(StatisticInterval.HOUR), StatisticInterval.HOUR), is(getTimeStr(getNow(), StatisticInterval.HOUR)));
        assertThat(getTimeStr(StatisticTimeUtils.getCurrentStatisticTime(StatisticInterval.DAY), StatisticInterval.DAY), is(getTimeStr(getNow(), StatisticInterval.DAY)));
    }
    
    @Test
    public void assertGetStatisticTime() {
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(StatisticInterval.MINUTE, -1), StatisticInterval.MINUTE), is(getTimeStr(getLastMinute(), StatisticInterval.MINUTE)));
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(StatisticInterval.HOUR, -1), StatisticInterval.HOUR), is(getTimeStr(getLastHour(), StatisticInterval.HOUR)));
        assertThat(getTimeStr(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -1), StatisticInterval.DAY), is(getTimeStr(getYesterday(), StatisticInterval.DAY)));
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
    
    private String getTimeStr(final Date time, final StatisticInterval interval) {
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
