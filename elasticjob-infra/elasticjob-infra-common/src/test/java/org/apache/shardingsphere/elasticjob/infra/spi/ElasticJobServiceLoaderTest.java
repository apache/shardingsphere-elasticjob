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

package org.apache.shardingsphere.elasticjob.infra.spi;

import org.apache.shardingsphere.elasticjob.infra.spi.fixture.TypedFooService;
import org.apache.shardingsphere.elasticjob.infra.spi.fixture.UnRegisteredTypedFooService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ElasticJobServiceLoaderTest {
    
    @BeforeClass
    public static void register() {
        ElasticJobServiceLoader.registerTypedService(TypedFooService.class);
    }
    
    @Test
    public void assertGetCacheTypedService() {
        assertThat(ElasticJobServiceLoader.getCachedTypedServiceInstance(TypedFooService.class, "typedFooServiceImpl").orElse(null), instanceOf(TypedFooService.class));
    }
    
    @Test
    public void assertNewTypedServiceInstance() {
        assertThat(ElasticJobServiceLoader.getCachedTypedServiceInstance(TypedFooService.class, "typedFooServiceImpl").orElse(null), instanceOf(TypedFooService.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetCacheTypedServiceFailureWithUnRegisteredServiceInterface() {
        ElasticJobServiceLoader.getCachedTypedServiceInstance(UnRegisteredTypedFooService.class, "unRegisteredTypedFooServiceImpl").orElseThrow(IllegalArgumentException::new);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetCacheTypedServiceFailureWithInvalidType() {
        ElasticJobServiceLoader.getCachedTypedServiceInstance(TypedFooService.class, "INVALID").orElseThrow(IllegalArgumentException::new);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewTypedServiceInstanceFailureWithUnRegisteredServiceInterface() {
        ElasticJobServiceLoader.newTypedServiceInstance(UnRegisteredTypedFooService.class, "unRegisteredTypedFooServiceImpl", new Properties()).orElseThrow(IllegalArgumentException::new);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewTypedServiceInstanceFailureWithInvalidType() {
        ElasticJobServiceLoader.newTypedServiceInstance(TypedFooService.class, "INVALID", new Properties()).orElseThrow(IllegalArgumentException::new);
    }
}
