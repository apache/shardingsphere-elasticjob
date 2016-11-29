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

package com.dangdang.ddframe.job.executor.handler;

import com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 作业属性配置.
 *
 * @author zhangliang
 */
@AllArgsConstructor
@NoArgsConstructor
public final class JobProperties {
    
    private Map<JobPropertiesEnum, String> map = new LinkedHashMap<>(JobPropertiesEnum.values().length, 1);
    
    /**
     * 设置作业属性.
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void put(final String key, final String value) {
        JobPropertiesEnum jobPropertiesEnum = JobPropertiesEnum.from(key);
        if (null == jobPropertiesEnum || null == value) {
            return;
        }
        map.put(jobPropertiesEnum, value);
    }
    
    /**
     * 获取作业属性.
     * 
     * @param jobPropertiesEnum 作业属性枚举
     * @return 属性值
     */
    public String get(final JobPropertiesEnum jobPropertiesEnum) {
        return map.containsKey(jobPropertiesEnum) ? map.get(jobPropertiesEnum) : jobPropertiesEnum.getDefaultValue();
    }
    
    /**
     * 获取所有键.
     * 
     * @return 键集合
     */
    public String json() {
        Map<String, String> jsonMap = new LinkedHashMap<>(JobPropertiesEnum.values().length, 1);
        for (JobPropertiesEnum each : JobPropertiesEnum.values()) {
            jsonMap.put(each.getKey(), get(each));
        }
        return GsonFactory.getGson().toJson(jsonMap);
    }
    
    /**
     * 作业属性枚举.
     */
    @RequiredArgsConstructor
    @Getter
    public enum JobPropertiesEnum {
        
        /**
         * 作业异常处理器.
         */
        JOB_EXCEPTION_HANDLER("job_exception_handler", JobExceptionHandler.class, DefaultJobExceptionHandler.class.getCanonicalName()),
        
        /**
         * 线程池服务处理器.
         */
        EXECUTOR_SERVICE_HANDLER("executor_service_handler", ExecutorServiceHandler.class, DefaultExecutorServiceHandler.class.getCanonicalName());
        
        private final String key;
    
        private final Class<?> classType;
        
        private final String defaultValue;
        
        /**
         * 通过属性键获取枚举.
         * 
         * @param key 属性键
         * @return 枚举
         */
        public static JobPropertiesEnum from(final String key) {
            for (JobPropertiesEnum each : JobPropertiesEnum.values()) {
                if (each.getKey().equals(key)) {
                    return each;
                }
            }
            return null;
        }
    }
}
