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

package com.dangdang.ddframe.job.lite.api.config;

import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.dangdang.ddframe.job.api.simple.SimpleElasticJob;
import com.dangdang.ddframe.job.lite.api.config.impl.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.SimpleJobConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业配置工厂.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationFactory {

    /**
     * 创建简单作业配置.
     *
     * @param jobName 作业名称
     * @param jobClass 作业实现类名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @return 简单作业配置
     */
    public static SimpleJobConfiguration.SimpleJobConfigurationBuilder createSimpleJobConfigurationBuilder(
            final String jobName, final Class<? extends SimpleElasticJob> jobClass, final int shardingTotalCount, final String cron) {
        return new SimpleJobConfiguration.SimpleJobConfigurationBuilder(jobName, jobClass, shardingTotalCount, cron);
    }

    /**
     * 创建数据流作业配置.
     *
     * @param jobName 作业名称
     * @param jobClass 作业实现类名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @param dataflowType 数据流作业类型
     * @return 数据流作业配置
     */
    public static DataflowJobConfiguration.DataflowJobConfigurationBuilder createDataflowJobConfigurationBuilder(
            final String jobName, final Class<? extends DataflowElasticJob> jobClass, final int shardingTotalCount, final String cron, final DataflowType dataflowType) {
        return new DataflowJobConfiguration.DataflowJobConfigurationBuilder(jobName, jobClass, shardingTotalCount, cron, dataflowType);
    }

    /**
     * 创建脚本作业配置.
     *
     * @param jobName 作业名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @param scriptCommandLine 作业脚本命令行
     * @return 脚本作业配置
     */
    public static ScriptJobConfiguration.ScriptJobConfigurationBuilder createScriptJobConfigurationBuilder(
            final String jobName, final int shardingTotalCount, final String cron, final String scriptCommandLine) {
        return new ScriptJobConfiguration.ScriptJobConfigurationBuilder(jobName, shardingTotalCount, cron, scriptCommandLine);
    }
}
