/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.spring.job;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:META-INF/job/withListenerAndCglib.xml")
public final class JobSpringNamespaceWithListenerAndCglibTest extends AbstractJobSpringIntegrateTest {
    
    public JobSpringNamespaceWithListenerAndCglibTest() {
        super("simpleElasticJob_namespace_listener_cglib", "dataflowElasticJob_namespace_listener_cglib");
    }
}
