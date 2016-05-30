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

package com.dangdang.ddframe.job.api.config;

import com.dangdang.ddframe.job.internal.job.JobType;
import com.dangdang.ddframe.job.plugin.job.type.integrated.ScriptElasticJob;
import lombok.Getter;
import lombok.Setter;

/**
 * 脚本作业配置信息.
 * 
 * @author caohao
 */
@Getter
public class ScriptJobConfiguration extends BaseJobConfiguration<ScriptElasticJob> {

    private final JobType jobType = JobType.SCRIPT;

    private String scriptCommandLine;

    public ScriptJobConfiguration(final String jobName, final int shardingTotalCount, final String cron, final String scriptCommandLine) {
        super(jobName, ScriptElasticJob.class, shardingTotalCount, cron);
        this.scriptCommandLine = scriptCommandLine;
    }
}
