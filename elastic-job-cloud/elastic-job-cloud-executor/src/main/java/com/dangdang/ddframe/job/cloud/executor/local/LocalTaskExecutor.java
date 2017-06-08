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

package com.dangdang.ddframe.job.cloud.executor.local;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地作业执行器.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public final class LocalTaskExecutor {
    
    private final LocalCloudJobConfiguration localCloudJobConfiguration;
    
    /**
     * 本地执行作业.
     */
    public void execute() {
        switch (localCloudJobConfiguration.getTypeConfig().getJobType()) {
            case SIMPLE:
                processSimple();
                break;
            case DATAFLOW: 
                processDataflow();
                break;
            case SCRIPT:
                processScript();
                break;
            default:
                throw new UnsupportedOperationException(localCloudJobConfiguration.getTypeConfig().getJobType().name());
        }
    }
    
    private void processSimple() {
        getJobInstance(SimpleJob.class).execute(getShardingContext());
    }
    
    @SuppressWarnings("unchecked")
    private void processDataflow() {
        ShardingContext shardingContext = getShardingContext();
        DataflowJob<Object> dataflowJob = getJobInstance(DataflowJob.class);
        List<Object> data = dataflowJob.fetchData(shardingContext);
        if (null != data && !data.isEmpty()) {
            dataflowJob.processData(shardingContext, data);
        }
    }
    
    private void processScript() {
        String scriptCommandLine = ((ScriptJobConfiguration) localCloudJobConfiguration.getTypeConfig()).getScriptCommandLine();
        if (Strings.isNullOrEmpty(scriptCommandLine)) {
            throw new JobConfigurationException("Cannot find script command line for job '%s', job is not executed.", localCloudJobConfiguration.getJobName());
        }
        CommandLine commandLine = CommandLine.parse(scriptCommandLine);
        commandLine.addArgument(GsonFactory.getGson().toJson(getShardingContext()), false);
        try {
            new DefaultExecutor().execute(commandLine);
        } catch (final IOException ex) {
            throw new JobConfigurationException("Execute script failure.", ex);
        }
    }
    
    private <T extends ElasticJob> T getJobInstance(final Class<T> clazz) {
        Object result;
        if (Strings.isNullOrEmpty(localCloudJobConfiguration.getApplicationContext())) {
            String jobClass = localCloudJobConfiguration.getTypeConfig().getJobClass();
            try {
                result = Class.forName(jobClass).newInstance();
            } catch (final ReflectiveOperationException ex) {
                throw new JobSystemException("Elastic-Job: Class '%s' initialize failure, the error message is '%s'.", jobClass, ex.getMessage());
            }
        } else {
            result = new ClassPathXmlApplicationContext(localCloudJobConfiguration.getApplicationContext()).getBean(localCloudJobConfiguration.getBeanName());
        }
        return clazz.cast(result);
    }
    
    private ShardingContext getShardingContext() {
        JobCoreConfiguration coreConfig = localCloudJobConfiguration.getTypeConfig().getCoreConfig();
        Map<Integer, String> shardingItemMap = new HashMap<>(1, 1);
        shardingItemMap.put(localCloudJobConfiguration.getShardingItem(), 
                new ShardingItemParameters(coreConfig.getShardingItemParameters()).getMap().get(localCloudJobConfiguration.getShardingItem()));
        return new ShardingContext(new ShardingContexts("foo_task_id", localCloudJobConfiguration.getJobName(), 
                coreConfig.getShardingTotalCount(), coreConfig.getJobParameter(), shardingItemMap), localCloudJobConfiguration.getShardingItem());
    }
}
