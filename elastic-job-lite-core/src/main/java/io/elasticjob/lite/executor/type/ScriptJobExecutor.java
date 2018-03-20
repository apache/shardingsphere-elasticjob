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

package io.elasticjob.lite.executor.type;

import com.google.common.base.Strings;
import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.config.script.ScriptJobConfiguration;
import io.elasticjob.lite.exception.JobConfigurationException;
import io.elasticjob.lite.executor.AbstractElasticJobExecutor;
import io.elasticjob.lite.executor.JobFacade;
import io.elasticjob.lite.util.json.GsonFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

/**
 * 脚本作业执行器.
 * 
 * @author zhangliang
 * @author caohao
 */
public final class ScriptJobExecutor extends AbstractElasticJobExecutor {
    
    public ScriptJobExecutor(final JobFacade jobFacade) {
        super(jobFacade);
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        final String scriptCommandLine = ((ScriptJobConfiguration) getJobRootConfig().getTypeConfig()).getScriptCommandLine();
        if (Strings.isNullOrEmpty(scriptCommandLine)) {
            throw new JobConfigurationException("Cannot find script command line for job '%s', job is not executed.", shardingContext.getJobName());
        }
        executeScript(shardingContext, scriptCommandLine);
    }
    
    private void executeScript(final ShardingContext shardingContext, final String scriptCommandLine) {
        CommandLine commandLine = CommandLine.parse(scriptCommandLine);
        commandLine.addArgument(GsonFactory.getGson().toJson(shardingContext), false);
        try {
            new DefaultExecutor().execute(commandLine);
        } catch (final IOException ex) {
            throw new JobConfigurationException("Execute script failure.", ex);
        }
    }
}
