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

package com.dangdang.ddframe.job.lite.spring.fixture.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleJdkDynamicProxyListener implements ElasticJobListener {
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        assertThat(shardingContexts.getJobName(), is("simpleElasticJob_namespace_listener_jdk_proxy"));
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        assertThat(shardingContexts.getJobName(), is("simpleElasticJob_namespace_listener_jdk_proxy"));
    }
}
