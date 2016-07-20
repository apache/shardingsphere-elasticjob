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

import com.dangdang.ddframe.job.api.type.integrated.ScriptElasticJob;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 脚本作业配置命名空间对象.
 *
 * @author caohao
 */
@Getter
@Setter
public class ScriptJobConfigurationDto extends AbstractJobConfigurationDto<ScriptJobConfiguration, ScriptElasticJob, ScriptJobConfiguration.ScriptJobConfigurationBuilder> {
    
    // TODO  scriptCommandLine支持classpath:方式
    private String scriptCommandLine;
    
    ScriptJobConfigurationDto(final String jobName, final Integer shardingTotalCount, final String cron, final String scriptCommandLine) {
        super(jobName, JobType.SCRIPT, ScriptElasticJob.class, shardingTotalCount, cron);
        this.scriptCommandLine = scriptCommandLine;
    }
    
    @Override
    public ScriptJobConfiguration toJobConfiguration() {
        return createBuilder().build();
    }
    
    @Override
    protected ScriptJobConfiguration.ScriptJobConfigurationBuilder createCustomizedBuilder() {
        return new ScriptJobConfiguration.ScriptJobConfigurationBuilder(getJobName(), getShardingTotalCount(), getCron(), getScriptCommandLine());
    } 
    
    @Override
    protected ScriptJobConfiguration.ScriptJobConfigurationBuilder buildCustomizedProperties(final ScriptJobConfiguration.ScriptJobConfigurationBuilder builder) {
        if (null != getScriptCommandLine()) {
            builder.scriptCommandLine(getScriptCommandLine());    
        }
        return builder;
    }
}
