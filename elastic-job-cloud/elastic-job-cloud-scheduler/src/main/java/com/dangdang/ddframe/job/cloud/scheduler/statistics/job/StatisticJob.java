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

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * 统计作业.
 *
 * @author liguangyun
 */
public interface StatisticJob extends Job {
    
    /**
     * 构建JobDetail.
     * 
     * @return JobDetail对象
     */
    JobDetail buildJobDetail();
    
    /**
     * 构建Trigger.
     * 
     * @return Trigger对象
     */
    Trigger buildTrigger();
    
    /**
     * 获取对象属性Map.
     * 
     * @return 对象属性Map，KEY为属性名称，VALUE为属性实例
     */
    Map<String, Object> getDataMap();
}
