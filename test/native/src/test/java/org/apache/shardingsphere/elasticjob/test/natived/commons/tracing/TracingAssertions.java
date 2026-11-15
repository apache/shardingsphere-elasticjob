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

package org.apache.shardingsphere.elasticjob.test.natived.commons.tracing;

import org.awaitility.Awaitility;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * Assertions on the RDB tracing events of jobs in native tests.
 */
public final class TracingAssertions {
    
    private TracingAssertions() {
    }
    
    /**
     * Wait until the job has a successful execution in the RDB tracing events.
     *
     * @param dataSource data source of the RDB tracing
     * @param jobName job name
     */
    public static void awaitSuccessfulExecution(final DataSource dataSource, final String jobName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Awaitility.await().atMost(Duration.ofSeconds(60)).ignoreExceptions()
                .until(() -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JOB_EXECUTION_LOG WHERE job_name = ? AND is_success = 1", Integer.class, jobName) > 0);
    }
}
