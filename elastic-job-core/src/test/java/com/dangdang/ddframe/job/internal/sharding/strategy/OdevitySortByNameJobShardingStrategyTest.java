package com.dangdang.ddframe.job.internal.sharding.strategy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public final class OdevitySortByNameJobShardingStrategyTest {
    
    private OdevitySortByNameJobShardingStrategy odevitySortByNameJobShardingStrategy = new OdevitySortByNameJobShardingStrategy();
    
    @Test
    public void assertshardingByAsc() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(0));
        expected.put("host1", Arrays.asList(1));
        expected.put("host2", Collections.<Integer>emptyList());
        assertThat(
                odevitySortByNameJobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), new JobShardingStrategyOption("1", 2, Collections.<Integer, String>emptyMap())), is(expected));
    }
    
    @Test
    public void assertshardingByDesc() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host2", Arrays.asList(0));
        expected.put("host1", Arrays.asList(1));
        expected.put("host0", Collections.<Integer>emptyList());
        assertThat(
                odevitySortByNameJobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), new JobShardingStrategyOption("0", 2, Collections.<Integer, String>emptyMap())), is(expected));
    }
}
