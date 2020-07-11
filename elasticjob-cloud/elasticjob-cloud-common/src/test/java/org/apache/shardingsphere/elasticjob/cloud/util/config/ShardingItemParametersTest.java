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

import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingItemParametersTest {
    
    @Test(expected = JobConfigurationException.class)
    public void assertNewWhenPairFormatInvalid() {
        new ShardingItemParameters("xxx-xxx");
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertNewWhenItemIsNotNumber() {
        new ShardingItemParameters("xxx=xxx");
    }
    
    @Test
    public void assertGetMapWhenIsEmpty() {
        assertThat(new ShardingItemParameters("").getMap(), is(Collections.EMPTY_MAP));
    }
    
    @Test
    public void assertGetMap() {
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(new ShardingItemParameters("0=A,1=B,2=C").getMap(), is(expected));
    }
}
