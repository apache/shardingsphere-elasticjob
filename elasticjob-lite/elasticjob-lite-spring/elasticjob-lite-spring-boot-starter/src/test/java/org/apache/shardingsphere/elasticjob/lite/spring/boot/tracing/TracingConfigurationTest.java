package org.apache.shardingsphere.elasticjob.lite.spring.boot.tracing;

import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@SpringBootTest
@SpringBootApplication
public class TracingConfigurationTest extends AbstractJUnit4SpringContextTests {

    @Test
    public void assertNotRDBConfiguration() {
        assertNotNull(applicationContext);
        assertFalse(applicationContext.containsBean("tracingDataSource"));
        ObjectProvider<Object> provider = applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(TracingConfiguration.class, DataSource.class));
        assertNull(provider.getIfAvailable());
    }
}
