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

package org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture.repository;

import org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture.entity.TOrderPOJO;
import org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture.entity.TableStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class VirtualTOrderRepository {
    private final Map<Long, TOrderPOJO> data = new ConcurrentHashMap<>(300, 1);

    public VirtualTOrderRepository() {
        addData(0L, 100L, "Norddorf");
        addData(100L, 200L, "Bordeaux");
        addData(200L, 300L, "Somerset");
    }

    private void addData(final long startId, final long endId, final String location) {
        LongStream.range(startId, endId).forEachOrdered(i -> data.put(i, new TOrderPOJO(i, location, TableStatus.TODO)));
    }

    /**
     * Query operations on virtual tables.
     *
     * @param location Virtual table location attribute
     * @param limitNumber Number of items queried
     * @return Deserialized object for multiple columns of table
     */
    public List<TOrderPOJO> findTodoData(final String location, final int limitNumber) {
        return data.entrySet().stream()
                .limit(limitNumber)
                .map(Map.Entry::getValue)
                .filter(tOrderPOJO -> location.equals(tOrderPOJO.getLocation()) && TableStatus.TODO == tOrderPOJO.getTableStatus())
                .collect(Collectors.toCollection(() -> new ArrayList<>(limitNumber)));
    }

    /**
     * Set the tableStatus attribute of a column in the virtual table.
     *
     * @param id Virtual table id attribute
     */
    public void setCompleted(final long id) {
        data.replace(id, new TOrderPOJO(id, data.get(id).getLocation(), TableStatus.COMPLETED));
    }
}
