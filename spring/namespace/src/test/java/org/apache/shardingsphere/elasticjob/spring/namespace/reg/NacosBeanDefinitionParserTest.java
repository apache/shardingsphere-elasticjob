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

import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NacosBeanDefinitionParserTest {
    
    @Test
    void assertNacosRegistryCenterBeanDefinition() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context).loadBeanDefinitions("classpath:META-INF/reg/nacos.xml");
        BeanDefinition actual = context.getBeanDefinition("nacosRegistryCenter");
        assertThat(actual.getBeanClassName(), is(NacosRegistryCenter.class.getName()));
        assertThat(actual.getInitMethodName(), is("init"));
        assertThat(actual.getDestroyMethodName(), is("close"));
        NacosConfiguration config = instantiateConfiguration(actual);
        assertThat(config.getServerLists(), is("127.0.0.1:8848,127.0.0.2:8848"));
        assertThat(config.getNamespace(), is("elasticjob-namespace"));
        assertThat(config.getTenant(), is("public-namespace"));
        assertThat(config.getUsername(), is("nacos"));
        assertThat(config.getPassword(), is("nacos"));
        assertThat(config.getTimeoutMs(), is(5000L));
    }
    
    private NacosConfiguration instantiateConfiguration(final BeanDefinition beanDefinition) {
        AbstractBeanDefinition configDefinition = (AbstractBeanDefinition) beanDefinition.getConstructorArgumentValues().getIndexedArgumentValues().get(0).getValue();
        GenericApplicationContext configContext = new GenericApplicationContext();
        configContext.registerBeanDefinition("nacosConfiguration", configDefinition);
        configContext.refresh();
        return configContext.getBean("nacosConfiguration", NacosConfiguration.class);
    }
}
