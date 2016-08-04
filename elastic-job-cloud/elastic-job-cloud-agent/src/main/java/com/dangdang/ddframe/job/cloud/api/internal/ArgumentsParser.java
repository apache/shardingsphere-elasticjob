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

package com.dangdang.ddframe.job.cloud.api.internal;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJob;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 命令行参数解析器.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ArgumentsParser {
    
    private ElasticJob elasticJob;
    
    private ShardingContext shardingContext;
    
    private JobConfigurationContext jobConfig;
    
    /**
     * 解析.
     * @param args 命令行参数
     * @return 解析对象
     * @throws JobExecutionEnvironmentException 作业执行环境异常
     */
    public static ArgumentsParser parse(final String[] args) throws JobExecutionEnvironmentException {
        int argumentsLength = 2;
        if (argumentsLength != args.length) {
            throw new JobExecutionEnvironmentException("Elastic-Job: Arguments parse failure, should have %s arguments.", argumentsLength);
        }
        ArgumentsParser result = new ArgumentsParser();
        result.jobConfig = new JobConfigurationContext(GsonFactory.getGson().fromJson(args[1], Map.class));
        String jobClass = result.jobConfig.getTypeConfig().getJobClass();
        try {
            Class<?> elasticJobClass = Class.forName(jobClass);
            if (!ElasticJob.class.isAssignableFrom(elasticJobClass)) {
                throw new JobExecutionEnvironmentException("Elastic-Job: Class '%s' must implements ElasticJob interface.", jobClass);
            }
            if (elasticJobClass != ScriptJob.class) {
                result.elasticJob = (ElasticJob) elasticJobClass.newInstance();
            }
        } catch (final ReflectiveOperationException ex) {
            throw new JobExecutionEnvironmentException("Elastic-Job: Class '%s' initialize failure, the error message is '%s'.", jobClass, ex.getMessage());
        }
        result.shardingContext = GsonFactory.getGson().fromJson(args[0], ShardingContext.class);
        return result;
    }
}
