/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.lite.api.config;

import com.dangdang.ddframe.job.lite.api.DataFlowElasticJob;
import com.dangdang.ddframe.job.lite.api.config.impl.DataFlowJobConfiguration.DataFlowJobConfigurationBuilder;
import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration.ScriptJobConfigurationBuilder;
import com.dangdang.ddframe.job.lite.api.config.impl.SimpleJobConfiguration.SimpleJobConfigurationBuilder;
import com.dangdang.ddframe.job.lite.plugin.job.type.simple.AbstractSimpleElasticJob;

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
    public static SimpleJobConfigurationBuilder createSimpleJobConfigurationBuilder(final String jobName,
                                                                                    final Class<? extends AbstractSimpleElasticJob> jobClass, final int shardingTotalCount, final String cron) {
        return new SimpleJobConfigurationBuilder(jobName, jobClass, shardingTotalCount, cron);
    }

    /**
     * 创建数据流作业配置.
     *
     * @param jobName 作业名称
     * @param jobClass 作业实现类名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @return 数据流作业配置
     */
    public static DataFlowJobConfigurationBuilder createDataFlowJobConfigurationBuilder(final String jobName,
                                                                                        final Class<? extends DataFlowElasticJob> jobClass, final int shardingTotalCount, final String cron) {
        return new DataFlowJobConfigurationBuilder(jobName, jobClass, shardingTotalCount, cron);
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
    public static ScriptJobConfigurationBuilder createScriptJobConfigurationBuilder(final String jobName, final int shardingTotalCount,
                                                                                    final String cron, final String scriptCommandLine) {
        return new ScriptJobConfigurationBuilder(jobName, shardingTotalCount, cron, scriptCommandLine);
    }
}
