package com.dangdang.ddframe.job.cloud.internal.sharding.strategy;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dangdang.ddframe.job.cloud.exception.JobShardingStrategyClassConfigurationException;
import com.dangdang.ddframe.job.cloud.internal.sharding.strategy.fixture.InvalidJobShardingStrategy;
import com.dangdang.ddframe.job.cloud.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;

public class JobShardingStrategyFactoryTest {
    
    @Test
    public void assertGetDefaultStrategy() {
        assertThat(JobShardingStrategyFactory.getStrategy(null), instanceOf(AverageAllocationJobShardingStrategy.class));
    }
    
    @Test(expected = JobShardingStrategyClassConfigurationException.class)
    public void assertGetStrategyFailureWhenClassNotFound() {
        JobShardingStrategyFactory.getStrategy("NotClass");
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetStrategyFailureWhenNotStrategyClass() {
        JobShardingStrategyFactory.getStrategy(Object.class.getName());
    }
    
    @Test(expected = JobShardingStrategyClassConfigurationException.class)
    public void assertGetStrategyFailureWhenStrategyClassInvalid() {
        JobShardingStrategyFactory.getStrategy(InvalidJobShardingStrategy.class.getName());
    }
    
    @Test
    public void assertGetStrategySuccess() {
        assertThat(JobShardingStrategyFactory.getStrategy(AverageAllocationJobShardingStrategy.class.getName()), instanceOf(AverageAllocationJobShardingStrategy.class));
    }
}
