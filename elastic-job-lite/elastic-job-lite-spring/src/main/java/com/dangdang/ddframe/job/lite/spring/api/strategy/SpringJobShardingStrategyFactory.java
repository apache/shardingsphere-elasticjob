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
package com.dangdang.ddframe.job.lite.spring.api.strategy;

import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyFactory;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 从spring 容器中加载策略分片类
 * @author leizhenyu
 */
@Slf4j
public class SpringJobShardingStrategyFactory implements JobShardingStrategyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringJobShardingStrategyFactory.class);

    private static final Set<ApplicationContext> contexts = new HashSet<>();

    @Override
    public JobShardingStrategy getStrategy(String strategyClassName) {
        if (Strings.isNullOrEmpty(strategyClassName)) {
            return null;
        }
        if (!CollectionUtils.isEmpty(contexts)) {
            for (ApplicationContext context : contexts) {
                try {
                    Object bean = context.getBean(Class.forName(strategyClassName));
                    return (JobShardingStrategy) bean;
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Load job sharding strategy class {} failed,cause class not found.", strategyClassName);
                    throw new JobConfigurationException("Class '%s' load Failed", strategyClassName);
                }catch (NoSuchBeanDefinitionException e){
                    //spring 容器的行为，如果不存在此类型的bean，直接抛异常，没有更优的办法，只好catch了，并且吞掉异常
                    log.debug(String.format("No this class bean %s defined in spring container.",strategyClassName));
                    return null;
                }
            }
        }
        return null;
    }

    public static void addApplicationContext(ApplicationContext context) {
        if (context != null) {
            contexts.add(context);
        }
    }


}
