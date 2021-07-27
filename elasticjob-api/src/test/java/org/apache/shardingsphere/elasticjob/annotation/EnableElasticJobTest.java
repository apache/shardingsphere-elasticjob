package org.apache.shardingsphere.elasticjob.annotation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.shardingsphere.elasticjob.annotation.job.impl.SimpleTestJob;
import org.junit.Test;

public class EnableElasticJobTest {
    
    @Test
    public void assertAnnotationJob() {
        EnableElasticJob annotation = SimpleTestJob.class.getAnnotation(EnableElasticJob.class);
        assertEquals(annotation.jobName(), "SimpleTestJob");
        assertEquals(annotation.cron(), "0/5 * * * * ?");
        assertEquals(annotation.shardingTotalCount(), 3);
        assertEquals(annotation.shardingItemParameters(), "0=Beijing,1=Shanghai,2=Guangzhou");
        assertArrayEquals(annotation.jobListenerTypes(), new String[] {"NOOP", "LOG"});
        for (ElasticJobProp prop :annotation.props()) {
            assertEquals(prop.key(), "print.title");
            assertEquals(prop.value(), "test title");
        }
    }
}
