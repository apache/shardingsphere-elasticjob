/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.example.elasticjob.fixture.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.dangdang.example.elasticjob.fixture.entity.Foo;

public final class FooRepositoryTest {
    
    private FooRepository fooRepository = new FooRepository();
    
    @Test
    public void assertFindActiveForShardingItems0() {
        assertFoo(Collections.singletonList(0), Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L));
    }
    
    @Test
    public void assertFindActiveForShardingItems1() {
        assertFoo(Collections.singletonList(1), Arrays.asList(10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L));
    }
    
    @Test
    public void assertFindActiveForShardingItems2And3() {
        assertFoo(Arrays.asList(2, 3), Arrays.asList(20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L));
    }
    
    private void assertFoo(final List<Integer> shardingItems, final List<Long> idList) {
        List<Foo> actual = fooRepository.findActive(shardingItems);
        assertThat(actual.size(), is(idList.size()));
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getId(), is(idList.get(i)));
        }
    }
}
