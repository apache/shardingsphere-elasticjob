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
import com.dangdang.ddframe.job.api.exception.JobConfigurationException;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 命令行参数解析器.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ArgumentsParser {
    
    private Class<? extends ElasticJob> elasticJobClass;
    
    private ShardingContext shardingContext;
    
    private CloudJobConfiguration jobConfig;
    
    /**
     * 解析.
     * @param args 命令行参数
     * @return 解析对象
     */
    @SuppressWarnings("unchecked")
    public static ArgumentsParser parse(final String[] args) {
        ArgumentsParser result = new ArgumentsParser();
        try {
            result.elasticJobClass = (Class<? extends ElasticJob>) Class.forName(args[0]);
        } catch (final ClassNotFoundException ex) {
            throw new JobConfigurationException(ex);
        }
        result.shardingContext = GsonFactory.getGson().fromJson(args[1], ShardingContext.class);
        result.jobConfig = CloudJobConfigurationGsonFactory.fromJson(args[2]);
        return result;
    }
}
