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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.dangdang.example.elasticjob.fixture.entity.Foo;
import com.dangdang.example.elasticjob.fixture.entity.FooStatus;

@Repository
public class FooRepository {
    
    private Map<Long, Foo> map = new ConcurrentHashMap<>(100);
    
    public FooRepository() {
        init();
    }
    
    private void init() {
        for (long i = 0; i < 100; i++) {
            map.put(i, new Foo(i, FooStatus.ACTIVE));
        }
    }
    
    public List<Foo> findActive(final List<Integer> shardingItems) {
        List<Foo> result = new ArrayList<>(shardingItems.size() * 10);
        for (int each : shardingItems) {
            result.addAll(findActive(each));
        }
        return result;
    }
    
    private List<Foo> findActive(final int shardingItem) {
        List<Foo> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            Foo foo = map.get((long) (shardingItem * 10 + i));
            if (FooStatus.ACTIVE == foo.getStatus()) {
                result.add(foo);
            }
        }
        return result;
    }
    
    public void setInactive(final long id) {
        map.get(id).setStatus(FooStatus.INACTIVE);
    }
}
