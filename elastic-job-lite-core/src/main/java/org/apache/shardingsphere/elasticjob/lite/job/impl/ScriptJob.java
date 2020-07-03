/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.job.impl;

import com.google.common.base.Strings;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.type.simple.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.lite.job.TypedJob;
import org.apache.shardingsphere.elasticjob.lite.util.json.GsonFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Script job.
 */
public final class ScriptJob implements SimpleJob, TypedJob {
    
    public static final String SCRIPT_KEY = "script.command.line";
    
    private volatile String scriptCommandLine;
    
    @Override
    public void init(final Properties props) {
        scriptCommandLine = getScriptCommandLine(props);
    }
    
    private String getScriptCommandLine(final Properties props) {
        String result = props.getProperty(SCRIPT_KEY);
        if (Strings.isNullOrEmpty(result)) {
            throw new JobConfigurationException("Cannot find script command line, job is not executed.");
        }
        return result;
    }
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        CommandLine commandLine = CommandLine.parse(scriptCommandLine);
        commandLine.addArgument(GsonFactory.getGson().toJson(shardingContext), false);
        try {
            new DefaultExecutor().execute(commandLine);
        } catch (final IOException ex) {
            throw new JobSystemException("Execute script failure.", ex);
        }
    }
    
    @Override
    public String getType() {
        return "SCRIPT";
    }
}
