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

package org.apache.shardingsphere.elasticjob.lite.job;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Typed job factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypedJobFactory {
    
    private static final Map<String, TypedJob> JOBS = new HashMap<>();
    
    static {
        for (TypedJob each : ServiceLoader.load(TypedJob.class)) {
            JOBS.put(each.getType(), each);
        }
    }
    
    /**
     * Get job.
     * 
     * @param type job type
     * @param props job properties
     * @return job
     */
    public static TypedJob getJob(final String type, final Properties props) {
        if (!JOBS.containsKey(type)) {
            throw new JobConfigurationException("Can not find job type `%s`.", type);
        }
        TypedJob result = JOBS.get(type);
        result.init(props);
        return result; 
    }
}
