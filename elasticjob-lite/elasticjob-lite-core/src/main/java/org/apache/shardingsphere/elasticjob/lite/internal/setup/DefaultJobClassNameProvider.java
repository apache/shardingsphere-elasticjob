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

package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;

/**
 * Simple job class name provider.
 */
public final class DefaultJobClassNameProvider implements JobClassNameProvider {
    
    private static final String LAMBDA_CHARACTERISTICS = "$$Lambda$";
    
    @Override
    public String getJobClassName(final ElasticJob elasticJob) {
        Class<? extends ElasticJob> elasticJobClass = elasticJob.getClass();
        String elasticJobClassName = elasticJobClass.getName();
        return isLambdaClass(elasticJobClass) ? trimLambdaClassSuffix(elasticJobClassName) : elasticJobClassName;
    }
    
    private boolean isLambdaClass(final Class<? extends ElasticJob> elasticJobClass) {
        return elasticJobClass.isSynthetic() && elasticJobClass.getSimpleName().contains(LAMBDA_CHARACTERISTICS);
    }
    
    private String trimLambdaClassSuffix(final String className) {
        return className.substring(0, className.lastIndexOf(LAMBDA_CHARACTERISTICS) + LAMBDA_CHARACTERISTICS.length());
    }
}
