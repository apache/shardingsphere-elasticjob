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
package com.dangdang.ddframe.job.lite.api.strategy;

import com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author leizhenyu
 */
public class JobShardingStrategyService {

    private final ServiceLoader<JobShardingStrategyFactory> jobShardingStrategyFactoryServiceLoader;

    private final List<JobShardingStrategyFactory> factories = Lists.newArrayList();

    public JobShardingStrategyService(){
        jobShardingStrategyFactoryServiceLoader = ServiceLoader.load(JobShardingStrategyFactory.class);
        init();
    }

    private void init(){
        initJobShardingStrategyFactory();
    }

    /**
     * 从serviceloader 中获取job分片工厂类
     * 路径是 /META-INF/services
     * @return job 分片类策略工厂
     */
    private void initJobShardingStrategyFactory(){
        List<JobShardingStrategyFactory> jobShardingStrategyFactories = Lists.newArrayList();
        JobShardingStrategyFactory failBackJobShardingStrgtegyFactory = null;

        //将默认的策略分片工厂类作为fallback，加在list的最后面。避免出现默认的排在第一而所有的策略类被默认的工厂类初始化
        for(JobShardingStrategyFactory factory : jobShardingStrategyFactoryServiceLoader){
            if(factory != null) {
                if(!(factory instanceof DefaultJobShardingStrategyFactory)) {
                    factories.add(factory);
                }else {
                    failBackJobShardingStrgtegyFactory = factory;
                }
            }
        }

        if(failBackJobShardingStrgtegyFactory != null) {
            factories.add(failBackJobShardingStrgtegyFactory);
        }
    }

    /**
     * 根据类名，获取job分片实例，如果找不到，就直接返回默认的。
     * @param stragetyClassName
     * @return
     */
    public JobShardingStrategy getJobShardingStrategy(String stragetyClassName){
        if (factories == null || factories.isEmpty()){
            //return default.
            return new AverageAllocationJobShardingStrategy();
        }
        for(JobShardingStrategyFactory factory : factories){
            JobShardingStrategy jobShardingStrategy = factory.getStrategy(stragetyClassName);
            if (jobShardingStrategy != null){
                return jobShardingStrategy;
            }
        }

        return new AverageAllocationJobShardingStrategy();
    }


}
