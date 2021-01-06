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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.job.util;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AopTargetUtilsTest {
    
    @Test
    public void assertJdkDynamicProxyForGetTarget() {
        ElasticJob target = new TargetJob();
        ProxyFactory pf = new ProxyFactory(target);
        pf.addInterface(ElasticJob.class);
        ElasticJob proxy = (ElasticJob) pf.getProxy();
        assertTrue(AopUtils.isJdkDynamicProxy(proxy));
        assertThat(AopTargetUtils.getTarget(proxy), is(target));
    }
    
    @Test
    public void assertCglibProxyForGetTarget() {
        ElasticJob target = new TargetJob();
        ProxyFactory pf = new ProxyFactory(target);
        pf.setProxyTargetClass(true);
        ElasticJob proxy = (ElasticJob) pf.getProxy();
        assertTrue(AopUtils.isCglibProxy(proxy));
        assertThat(AopTargetUtils.getTarget(proxy), is(target));
    }
    
    @Test
    public void assertNoneProxyForGetTarget() {
        ElasticJob proxy = new TargetJob();
        assertFalse(AopUtils.isAopProxy(proxy));
        assertThat(AopTargetUtils.getTarget(proxy), is(proxy));
    }
}
