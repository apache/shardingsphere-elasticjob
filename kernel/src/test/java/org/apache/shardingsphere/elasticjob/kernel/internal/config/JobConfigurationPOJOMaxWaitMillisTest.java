package org.apache.shardingsphere.elasticjob.kernel.internal.config;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobConfigurationPOJOMaxWaitMillisTest {

    @Test
    void assertMaxWaitMillisSurvivesRoundTrip() {
        JobConfiguration original = JobConfiguration.newBuilder("test_job", 1).maxWaitMillis(12345L).build();

        JobConfigurationPOJO pojo = JobConfigurationPOJO.fromJobConfiguration(original);
        assertEquals(12345L, pojo.getMaxWaitMillis(), "POJO should preserve maxWaitMillis when converting from JobConfiguration");

        JobConfiguration roundTripped = pojo.toJobConfiguration();
        assertEquals(12345L, roundTripped.getMaxWaitMillis(), "maxWaitMillis should survive a full round trip through JobConfigurationPOJO");
    }
}