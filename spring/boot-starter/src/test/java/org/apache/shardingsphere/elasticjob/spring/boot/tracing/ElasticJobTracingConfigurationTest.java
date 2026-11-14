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

package org.apache.shardingsphere.elasticjob.spring.boot.tracing;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ElasticJobTracingConfigurationTest {
    
    @Test
    void assertTracingDataSourceBoundFromProperties() {
        new ApplicationContextRunner()
                .withUserConfiguration(ElasticJobTracingConfiguration.class)
                .withPropertyValues(
                        "elasticjob.tracing.type=RDB",
                        "elasticjob.tracing.data-source.url=jdbc:h2:mem:tracing_binding;DB_CLOSE_DELAY=-1",
                        "elasticjob.tracing.data-source.username=sa",
                        "elasticjob.tracing.data-source.password=password",
                        "elasticjob.tracing.data-source.driver-class-name=org.h2.Driver")
                .run(context -> {
                    HikariDataSource tracingDataSource = context.getBean("tracingDataSource", HikariDataSource.class);
                    assertEquals("jdbc:h2:mem:tracing_binding;DB_CLOSE_DELAY=-1", tracingDataSource.getJdbcUrl());
                    assertEquals("sa", tracingDataSource.getUsername());
                    assertEquals("password", tracingDataSource.getPassword());
                    assertEquals("org.h2.Driver", tracingDataSource.getDriverClassName());
                    assertInstanceOf(TracingConfiguration.class, context.getBean(TracingConfiguration.class));
                });
    }
}
