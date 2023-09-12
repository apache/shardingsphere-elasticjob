/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.spring.boot.tracing;

import org.apache.shardingsphere.elasticjob.lite.spring.boot.job.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@SpringBootTest
@SpringBootApplication
@ActiveProfiles("tracing")
public class TracingConfigurationTest extends AbstractJUnit4SpringContextTests {

    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }

    @Test
    public void assertNotRDBConfiguration() {
        assertNotNull(applicationContext);
        assertFalse(applicationContext.containsBean("tracingDataSource"));
        ObjectProvider<Object> provider = applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(TracingConfiguration.class, DataSource.class));
        assertNull(provider.getIfAvailable());
    }
}
