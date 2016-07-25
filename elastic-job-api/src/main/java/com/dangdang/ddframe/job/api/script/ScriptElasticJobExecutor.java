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

package com.dangdang.ddframe.job.api.script;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.internal.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.JobFacade;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

/**
 * 简单作业执行器.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public final class ScriptElasticJobExecutor extends AbstractElasticJobExecutor {
    
    public ScriptElasticJobExecutor(final JobFacade jobFacade) {
        super(jobFacade);
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        String scriptCommandLine = getJobFacade().getScriptCommandLine();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(scriptCommandLine), "Cannot find script command line.");
        CommandLine commandLine = CommandLine.parse(scriptCommandLine);
        commandLine.addArgument(shardingContext.toJson(), false);
        try {
            new DefaultExecutor().execute(commandLine);
        } catch (final IOException ex) {
            handleException(ex);
        }
    }
}
