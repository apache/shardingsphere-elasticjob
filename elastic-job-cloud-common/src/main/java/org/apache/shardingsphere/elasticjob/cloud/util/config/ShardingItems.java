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

package org.apache.shardingsphere.elasticjob.cloud.util.config;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sharding items.
 */
@Getter
public final class ShardingItems {
    
    private static final String DELIMITER = ",";

    /**
     * Get sharding items via string.
     *
     * @param itemsString sharding items string
     * @return sharding items
     */
    public static List<Integer> toItemList(final String itemsString) {
        if (Strings.isNullOrEmpty(itemsString)) {
            return Collections.emptyList();
        }
        String[] items = itemsString.split(DELIMITER);
        List<Integer> result = new ArrayList<>(items.length);
        for (String each : items) {
            int item = Integer.parseInt(each);
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Get sharding items string.
     *
     * @param items sharding items
     * @return sharding items string
     */
    public static String toItemsString(final List<Integer> items) {
        return items.isEmpty() ? "" : Joiner.on(DELIMITER).join(items);
    }
}
