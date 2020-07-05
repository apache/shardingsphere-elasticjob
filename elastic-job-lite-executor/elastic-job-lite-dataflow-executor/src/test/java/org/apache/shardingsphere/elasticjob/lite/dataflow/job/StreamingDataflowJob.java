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

package org.apache.shardingsphere.elasticjob.lite.dataflow.job;

import org.apache.shardingsphere.elasticjob.lite.api.job.ShardingContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class StreamingDataflowJob implements DataflowJob<String> {
    
    private final Set<String> processedData = new CopyOnWriteArraySet<>();
    
    private final List<String> result = Arrays.asList("data0", "data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9");
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        return processedData.isEmpty() ? result : null;
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        processedData.addAll(data);
    }
    
    /**
     * Is completed.
     *
     * @return true if is completed
     */
    public boolean isCompleted() {
        return result.size() == processedData.size();
    }
}
