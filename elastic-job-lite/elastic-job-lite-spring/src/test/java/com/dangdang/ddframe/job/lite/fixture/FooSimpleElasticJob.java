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

package com.dangdang.ddframe.job.lite.fixture;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleElasticJob;
import lombok.Getter;
import lombok.Setter;

public final class FooSimpleElasticJob implements SimpleElasticJob {
    
    @Getter
    private static volatile boolean completed;
    
    @Setter
    private static String springValue;
    
    @Getter
    private static String jobValue;
    
    @Override
    public void execute(final ShardingContext context) {
        completed = true;
        jobValue = springValue;
    }
    
    public static void reset() {
        completed = false;
    }
}
