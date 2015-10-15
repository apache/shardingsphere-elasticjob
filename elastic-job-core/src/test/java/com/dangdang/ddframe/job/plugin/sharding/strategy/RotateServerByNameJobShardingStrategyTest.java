package com.dangdang.ddframe.job.plugin.sharding.strategy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyOption;

public class RotateServerByNameJobShardingStrategyTest {
    
    private RotateServerByNameJobShardingStrategy odevitySortByNameJobShardingStrategy = new RotateServerByNameJobShardingStrategy();
    
    @Test
    public void assertsharding1() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Collections.<Integer>emptyList());
        expected.put("host1", Arrays.asList(0));
        expected.put("host2", Arrays.asList(1));
        assertThat(
                odevitySortByNameJobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), new JobShardingStrategyOption("1", 2, Collections.<Integer, String>emptyMap())), is(expected));
    }
    
    @Test
    public void assertsharding2() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(1));
        expected.put("host1", Collections.<Integer>emptyList());
        expected.put("host2", Arrays.asList(0));
        assertThat(
                odevitySortByNameJobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), new JobShardingStrategyOption("2", 2, Collections.<Integer, String>emptyMap())), is(expected));
    }
    
    @Test
    public void assertsharding3() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(0));
        expected.put("host1", Arrays.asList(1));
        expected.put("host2", Collections.<Integer>emptyList());
        assertThat(
                odevitySortByNameJobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), new JobShardingStrategyOption("3", 2, Collections.<Integer, String>emptyMap())), is(expected));
    }
}
