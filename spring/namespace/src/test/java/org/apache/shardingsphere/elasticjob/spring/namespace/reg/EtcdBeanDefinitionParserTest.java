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

package org.apache.shardingsphere.elasticjob.spring.namespace.reg;

import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EtcdBeanDefinitionParserTest {
    
    @Test
    void assertEtcdRegistryCenterBeanDefinition() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context).loadBeanDefinitions("classpath:META-INF/reg/etcd.xml");
        BeanDefinition actual = context.getBeanDefinition("etcdRegistryCenter");
        assertThat(actual.getBeanClassName(), is(EtcdRegistryCenter.class.getName()));
        assertThat(actual.getInitMethodName(), is("init"));
        assertThat(actual.getDestroyMethodName(), is("close"));
        EtcdConfiguration config = instantiateConfiguration(actual);
        assertThat(config.getServerLists(), is("http://localhost:2379"));
        assertThat(config.getNamespace(), is("elasticjob-namespace"));
        assertThat(config.getConnectionTimeoutMilliseconds(), is(6000L));
        assertThat(config.getUsername(), is("root"));
        assertThat(config.getPassword(), is("password"));
        assertThat(config.isSsl(), is(true));
        assertThat(config.getAuthority(), is("etcd-server:2379"));
    }
    
    private EtcdConfiguration instantiateConfiguration(final BeanDefinition beanDefinition) {
        AbstractBeanDefinition configDefinition = (AbstractBeanDefinition) beanDefinition.getConstructorArgumentValues().getIndexedArgumentValues().get(0).getValue();
        GenericApplicationContext configContext = new GenericApplicationContext();
        configContext.registerBeanDefinition("etcdConfiguration", configDefinition);
        configContext.refresh();
        return configContext.getBean("etcdConfiguration", EtcdConfiguration.class);
    }
}
