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
 */

package com.dangdang.ddframe.job.cloud.plugin.job.type.integrated;

import com.dangdang.ddframe.job.cloud.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.cloud.exception.JobException;
import com.dangdang.ddframe.job.cloud.internal.job.AbstractElasticJob;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

/**
 * 脚本类型作业.
 *
 * @author caohao
 */
public final class ScriptElasticJob extends AbstractElasticJob {
    
    @Override
    protected void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        String scriptCommandLine = getJobFacade().getScriptCommandLine();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(scriptCommandLine), "Cannot find script command line.");
        CommandLine cmdLine = CommandLine.parse(scriptCommandLine);
        cmdLine.addArgument(shardingContext.toScriptArguments(), false);
        DefaultExecutor executor = new DefaultExecutor();
        try {
            executor.execute(cmdLine);
        } catch (final IOException ex) {
            throw new JobException(ex);
        }
    }
}
