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

package org.apache.shardingsphere.elasticjob.lite.spring;

import org.apache.shardingsphere.elasticjob.lite.spring.job.parser.JobBeanDefinitionParser;
import org.apache.shardingsphere.elasticjob.lite.spring.snapshot.parser.SnapshotBeanDefinitionParser;
import org.apache.shardingsphere.elasticjob.lite.spring.reg.parser.ZookeeperBeanDefinitionParser;
import org.apache.shardingsphere.elasticjob.lite.spring.tracing.parser.TracingBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Job handler for spring namespace.
 */
public final class ElasticJobNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser("job", new JobBeanDefinitionParser());
        registerBeanDefinitionParser("zookeeper", new ZookeeperBeanDefinitionParser());
        registerBeanDefinitionParser("snapshot", new SnapshotBeanDefinitionParser());
        registerBeanDefinitionParser("rdb-event-trace", new TracingBeanDefinitionParser());
    }
}
