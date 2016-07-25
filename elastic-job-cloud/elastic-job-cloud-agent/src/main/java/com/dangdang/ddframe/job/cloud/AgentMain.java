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

package com.dangdang.ddframe.job.cloud;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.JobFacade;
import com.dangdang.ddframe.job.api.script.ScriptElasticJob;
import com.dangdang.ddframe.job.api.script.ScriptElasticJobExecutor;
import com.dangdang.ddframe.job.api.simple.SimpleElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleElasticJobExecutor;
import com.dangdang.ddframe.job.cloud.api.CloudJobFacade;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 云作业启动执行器.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class AgentMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("conf/job.properties"));
            String[] jobClasses = properties.getProperty("job.classes").split(",");
            for (String each : jobClasses) {
                Class<?> cloudElasticJobClass = Class.forName(each);
                ElasticJob elasticJob = (ElasticJob) cloudElasticJobClass.getConstructor().newInstance();
                JobFacade jobFacade = new CloudJobFacade(GsonFactory.getGson().fromJson(args[0], ShardingContext.class));
                // TODO 与lite一起提炼
                AbstractElasticJobExecutor elasticJobExecutor;
                if (elasticJob instanceof SimpleElasticJob) {
                    elasticJobExecutor = new SimpleElasticJobExecutor((SimpleElasticJob) elasticJob, jobFacade);
                } else if (elasticJob instanceof DataflowElasticJob) {
                    elasticJobExecutor = new DataflowElasticJobExecutor((DataflowElasticJob) elasticJob, jobFacade);
                } else if (elasticJob instanceof ScriptElasticJob) {
                    elasticJobExecutor = new ScriptElasticJobExecutor(jobFacade);
                } else {
                    throw new JobException(String.format("Cannot support job type '%s'"), elasticJob.getClass());
                }
                elasticJobExecutor.execute();
            }
        }  catch (final IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
