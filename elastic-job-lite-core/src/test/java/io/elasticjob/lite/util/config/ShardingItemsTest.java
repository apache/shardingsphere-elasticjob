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

package io.elasticjob.lite.util.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingItemsTest {
    
    @Test
    public void assertTtoItemListWhenNull() {
        assertThat(ShardingItems.toItemList(null), is(Collections.EMPTY_LIST));
    }
    
    @Test
    public void assertToItemListWhenEmpty() {
        assertThat(ShardingItems.toItemList(""), is(Collections.EMPTY_LIST));
    }
    
    @Test
    public void assertToItemList() {
        assertThat(ShardingItems.toItemList("0,1,2"), is(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertToItemListForDuplicated() {
        assertThat(ShardingItems.toItemList("0,1,2,2"), is(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertToItemsStringWhenEmpty() {
        assertThat(ShardingItems.toItemsString(Collections.<Integer>emptyList()), is(""));
    }
    
    @Test
    public void assertToItemsString() {
        assertThat(ShardingItems.toItemsString(Arrays.asList(0, 1, 2)), is("0,1,2"));
    }
}
