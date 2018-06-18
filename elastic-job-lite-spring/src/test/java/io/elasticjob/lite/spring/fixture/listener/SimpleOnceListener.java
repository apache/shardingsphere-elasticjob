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

package io.elasticjob.lite.spring.fixture.listener;

import io.elasticjob.lite.executor.ShardingContexts;
import io.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import io.elasticjob.lite.spring.fixture.service.FooService;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleOnceListener extends AbstractDistributeOnceElasticJobListener {
    
    @Resource
    private FooService fooService;
    
    private final long startedTimeoutMilliseconds;
    
    private final long completedTimeoutMilliseconds;
    
    public SimpleOnceListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
        this.startedTimeoutMilliseconds = startedTimeoutMilliseconds;
        this.completedTimeoutMilliseconds = completedTimeoutMilliseconds;
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
        assertThat(startedTimeoutMilliseconds, is(10000L));
        assertThat(fooService.foo(), is("this is fooService."));
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        assertThat(completedTimeoutMilliseconds, is(20000L));
    }
}
