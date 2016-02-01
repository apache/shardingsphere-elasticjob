/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.spring.failure;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dangdang.ddframe.test.NestedZookeeperServers;

public final class ClassNotElasticJobTest {
    
    @Before
    public void setUp() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted();
    }
    
    @SuppressWarnings("resource")
    @Test(expected = BeanCreationException.class)
    public void assertNotElasticJob() {
        try {
            new ClassPathXmlApplicationContext("classpath:META-INF/job/classNotElasticJob.xml");
        } catch (final BeanCreationException ex) {
            throw ex;
        }
    }
}
