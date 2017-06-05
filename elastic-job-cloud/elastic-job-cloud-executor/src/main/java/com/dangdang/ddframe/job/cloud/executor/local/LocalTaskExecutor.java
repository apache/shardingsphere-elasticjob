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

import static com.dangdang.ddframe.job.api.JobType.DATAFLOW;
import static com.dangdang.ddframe.job.api.JobType.SIMPLE;

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
        if (SIMPLE == localCloudJobConfiguration.getTypeConfig().getJobType()) {
            getJobInstance(SimpleJob.class).execute(getShardingContext());
        } else if (DATAFLOW == localCloudJobConfiguration.getTypeConfig().getJobType()) {
            processDataflow();
        } else {
            processScript();
        }
    }
    
    private <T extends ElasticJob> T getJobInstance(final Class<T> clazz) {
        if (Strings.isNullOrEmpty(localCloudJobConfiguration.getApplicationContext())) {
            String jobClass = localCloudJobConfiguration.getTypeConfig().getJobClass();
            try {
                return clazz.cast(Class.forName(jobClass).newInstance());
            } catch (final ReflectiveOperationException ex) {
                throw new JobSystemException("Elastic-Job: Class '%s' initialize failure, the error message is '%s'.", jobClass, ex.getMessage());
            }
        } else {
            return clazz.cast(new ClassPathXmlApplicationContext(localCloudJobConfiguration.getApplicationContext()).getBean(localCloudJobConfiguration.getBeanName()));
        }
    }
    
    private ShardingContext getShardingContext() {
        JobCoreConfiguration coreConfig = localCloudJobConfiguration.getTypeConfig().getCoreConfig();
        String shardingItem = new ShardingItemParameters(coreConfig.getShardingItemParameters()).getMap().get(localCloudJobConfiguration.getShardingItem());
        Map<Integer, String> shardingItemMap = new HashMap<>(1);
        if (!Strings.isNullOrEmpty(shardingItem)) {
            shardingItemMap.put(localCloudJobConfiguration.getShardingItem(), shardingItem);
        }
        return new ShardingContext(new ShardingContexts("foo", localCloudJobConfiguration.getJobName(), coreConfig
                .getShardingTotalCount(), coreConfig.getJobParameter(), shardingItemMap), localCloudJobConfiguration.getShardingItem());
    }
    
    @SuppressWarnings("unchecked")
    private void processDataflow() {
        final ShardingContext shardingContext = getShardingContext();
        DataflowJob<Object> dataflowJob = getJobInstance(DataflowJob.class);
        List<Object> data = dataflowJob.fetchData(shardingContext);
        if (null != data && !data.isEmpty()) {
            dataflowJob.processData(shardingContext, data);
        }
    }
    
    private void processScript() {
        final String scriptCommandLine = ((ScriptJobConfiguration) localCloudJobConfiguration.getTypeConfig()).getScriptCommandLine();
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
}
