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

package org.apache.shardingsphere.elasticjob.spring.boot.job;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElasticJobBootstrapConfigurationTest {
    
    @Test
    void assertMoreThanOneTracingConfiguration() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ElasticJobAutoConfiguration.class))
                .withUserConfiguration(ExtraTracingConfiguration.class)
                .withPropertyValues(
                        "elasticjob.reg-center.type=memory",
                        "elasticjob.reg-center.namespace=elasticjob-bootstrap-configuration-test",
                        "elasticjob.tracing.type=RDB",
                        "elasticjob.tracing.data-source.url=jdbc:h2:mem:bootstrap_configuration;DB_CLOSE_DELAY=-1",
                        "elasticjob.tracing.data-source.username=sa",
                        "elasticjob.tracing.data-source.password=password",
                        "elasticjob.tracing.data-source.driver-class-name=org.h2.Driver")
                .run(context -> {
                    Throwable startupFailure = context.getStartupFailure();
                    assertNotNull(startupFailure);
                    assertThat(startupFailure.getMessage(),
                            containsString("More than one [org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration] beans found"));
                    assertThat(startupFailure.getMessage(), containsString("[elasticjob.tracing.type]"));
                });
    }
    
    @Configuration(proxyBeanMethods = false)
    static class ExtraTracingConfiguration {
        
        @Bean
        TracingConfiguration<DataSource> extraTracingConfiguration() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setJdbcUrl("jdbc:h2:mem:bootstrap_extra_tracing;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("password");
            return new TracingConfiguration<>("RDB", dataSource);
        }
    }
}
