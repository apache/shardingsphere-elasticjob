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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.dag;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.DagOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.DagBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.DagOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.junit.Test;

import java.util.List;

/**
 * test dag operate api.
 *
 **/
public class DagOperateTest {
    @Test
    public void test4DagJobDependencies() {
        String groupName = "DAGX";
        DagOperateAPI dagOperateAPI = new DagOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));

        List<DagBriefInfo> dagJobDependencies = dagOperateAPI.getDagJobDependencies(groupName);
        System.out.println(dagJobDependencies);
    }

    @Test
    public void test4DagStart() {
        String groupName = "DAGX";
        DagOperateAPI dagOperateAPI = new DagOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));

        dagOperateAPI.toggleDagStart(groupName);
    }

    @Test
    public void test4DagStop() {
        String groupName = "DAGX";
        DagOperateAPI dagOperateAPI = new DagOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));

        dagOperateAPI.toggleDagStop(groupName);
    }

    @Test
    public void test4DagPause() {
        String groupName = "DAGX";
        DagOperateAPI dagOperateAPI = new DagOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));

        dagOperateAPI.toggleDagPause(groupName);
    }

    @Test
    public void test4DagResume() {
        String groupName = "DAGX";
        DagOperateAPI dagOperateAPI = new DagOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));

        dagOperateAPI.toggleDagResume(groupName);
    }

    @Test
    public void test4RetiggerJob() {
        JobOperateAPI jobOperateAPI = new JobOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter("127.0.0.1:2181", "testb", ""));
        jobOperateAPI.trigger("JobA");
    }
}
