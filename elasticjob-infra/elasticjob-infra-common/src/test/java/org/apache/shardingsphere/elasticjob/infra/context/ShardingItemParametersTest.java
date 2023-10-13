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

package org.apache.shardingsphere.elasticjob.infra.context;

import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingItemParametersTest {
    
    @Test
    void assertNewWhenPairFormatInvalid() {
        assertThrows(JobConfigurationException.class, () -> new ShardingItemParameters("xxx-xxx"));
    }
    
    @Test
    void assertNewWhenItemIsNotNumber() {
        assertThrows(JobConfigurationException.class, () -> new ShardingItemParameters("xxx=xxx"));
    }
    
    @Test
    void assertGetMapWhenIsEmpty() {
        assertThat(new ShardingItemParameters("").getMap(), is(Collections.EMPTY_MAP));
    }
    
    @Test
    void assertGetMap() {
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(new ShardingItemParameters("0=A,1=B,2=C").getMap(), is(expected));
    }
}
