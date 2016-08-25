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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.script;

import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;
import lombok.Setter;

/**
 * 脚本作业配置命名空间对象.
 *
 * @author caohao
 * @author zhangliang
 */
@Setter
final class ScriptJobConfigurationDto extends AbstractJobConfigurationDto {
    
    // TODO scriptCommandLine支持classpath:方式
    private final String scriptCommandLine;
    
    ScriptJobConfigurationDto(final String jobName, final String cron, final int shardingTotalCount, final String scriptCommandLine) {
        super(jobName, cron, shardingTotalCount);
        this.scriptCommandLine = scriptCommandLine;
    }
    
    @Override
    protected JobTypeConfiguration toJobConfiguration(final JobCoreConfiguration jobCoreConfig) {
        return new ScriptJobConfiguration(jobCoreConfig, scriptCommandLine);
    }
}
