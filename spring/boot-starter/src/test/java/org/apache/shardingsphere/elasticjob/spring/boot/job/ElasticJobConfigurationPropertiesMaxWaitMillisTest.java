package org.apache.shardingsphere.elasticjob.spring.boot.job;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElasticJobConfigurationPropertiesMaxWaitMillisTest {

    @Test
    void assertMaxWaitMillisPropagatesToJobConfiguration() {
        ElasticJobConfigurationProperties properties = new ElasticJobConfigurationProperties();
        properties.setShardingTotalCount(3);
        properties.setMaxWaitMillis(45000L);

        JobConfiguration actual = properties.toJobConfiguration("test_job");

        assertEquals(45000L, actual.getMaxWaitMillis(), "maxWaitMillis set via Spring Boot properties should propagate to the built JobConfiguration");
    }

    @Test
    void assertMaxWaitMillisDefaultsWhenNotSet() {
        ElasticJobConfigurationProperties properties = new ElasticJobConfigurationProperties();
        properties.setShardingTotalCount(3);

        JobConfiguration actual = properties.toJobConfiguration("test_job");

        assertEquals(60_000L, actual.getMaxWaitMillis(), "maxWaitMillis should default to 60000ms when not explicitly configured via Spring Boot properties");
    }
}