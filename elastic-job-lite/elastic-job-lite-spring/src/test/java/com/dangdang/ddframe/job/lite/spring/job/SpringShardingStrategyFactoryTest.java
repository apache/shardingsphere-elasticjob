package com.dangdang.ddframe.job.lite.spring.job;

import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyService;
import com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;
import com.dangdang.ddframe.job.lite.spring.api.strategy.SpringJobShardingStrategyFactory;
import com.dangdang.ddframe.job.lite.spring.fixture.strategy.SpringSimpleStrategy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author leizhenyu
 */
@ContextConfiguration(locations = "/META-INF/job/withJobShardStrategyProperties.xml")
public class SpringShardingStrategyFactoryTest extends AbstractJUnit4SpringContextTests {

    private JobShardingStrategyService jobShardingStrategyService;

    @Before
    public void setUp() {
        jobShardingStrategyService = new JobShardingStrategyService();
        SpringJobShardingStrategyFactory.addApplicationContext(applicationContext);
    }

    @Test
    public void assertGetDefaultStrategy(){
        assertThat(jobShardingStrategyService.getJobShardingStrategy(null), instanceOf(AverageAllocationJobShardingStrategy.class));
    }

    @Test(expected = JobConfigurationException.class)
    public void assertGetStrategyFailureWhenClassNotFound() {
        jobShardingStrategyService.getJobShardingStrategy("NotClass");
    }

    @Test
    public void assertGetSuccessfulStrategy(){
        assertThat(jobShardingStrategyService.getJobShardingStrategy("com.dangdang.ddframe.job.lite.spring.fixture.strategy.SpringSimpleStrategy"),instanceOf(SpringSimpleStrategy.class));
    }

    @Test
    public void assertHasTwoStrategyFactoryClass()throws Exception{
        Field field = jobShardingStrategyService.getClass().getDeclaredField("factories");
        field.setAccessible(true);
        List lists =(List) field.get(new JobShardingStrategyService());
        assertEquals(lists.size(),2);
    }



}
