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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.shardingsphere.elasticjob.lite.internal.dag.DagJobStates;
import org.apache.shardingsphere.elasticjob.lite.internal.dag.DagNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.internal.dag.DagService;
import org.apache.shardingsphere.elasticjob.lite.internal.dag.DagStates;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.DagOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.DagBriefInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dag operate api implemention.
 *
 **/
@Slf4j
public class DagOperateAPIImpl implements DagOperateAPI {
    private final CoordinatorRegistryCenter regCenter;

    public DagOperateAPIImpl(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }

    @Override
    public boolean toggleDagPause(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.RUNNING) {
            log.error("Current dag-{} not RUNNING , Cannot turn to PAUSE", dagName);
            return false;
        }
        dagNodeStorage.updateDagStates(DagStates.PAUSE);
        return true;
    }

    @Override
    public boolean toggleDagResume(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.PAUSE) {
            log.error("Current dag-{} not PAUSE , Cannot turn to RUNNING", dagName);
            return false;
        }

        // get next should trigger job.
        DagService dagService = new DagService(regCenter, dagName, dagNodeStorage);
        List<String> nextJobs = dagService.nextShouldTriggerJob();
        dagNodeStorage.updateDagStates(DagStates.RUNNING);
        nextJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public boolean toggleDagStart(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates == DagStates.RUNNING || dagStates == DagStates.PAUSE) {
            log.error("Can not start DAG-{}, current state-{}", dagName, dagStates);
            return false;
        }
        // trigger self
        List<String> rootJobs = Lists.newArrayList();
        Map<String, Set<String>> allDagConfigJobs = dagNodeStorage.getAllDagConfigJobs();

        allDagConfigJobs.forEach((key, value) -> {
            if (value.size() == 1 && value.contains(DagService.ROOT_JOB)) {
                rootJobs.add(key);
            }
        });

        log.info("Dag-{} start jobs [{}]", dagName, rootJobs);
        rootJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public boolean toggleDagStop(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.PAUSE) {
            log.error("Current dag-{} not PAUSE , Cannot turn to FAIL", dagName);
            return false;
        }
        dagNodeStorage.updateDagStates(DagStates.FAIL);
        return true;
    }

    @Override
    public boolean toggleDagRerunWhenFail(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.FAIL) {
            log.error("Current dag-{} not FAIL , Cannot rerun fail jobs", dagName);
            return false;
        }

        // delete fail job
        List<String> failJobs = dagNodeStorage.getDagJobListByState(DagJobStates.FAIL);
        if (failJobs.isEmpty()) {
            log.error("Dag-{} don't have fail jobs", dagName);
            return false;
        }
        log.info("Dag-{} rerun fail jobs [{}]", dagName, failJobs);
        failJobs.forEach(job -> dagNodeStorage.removeFailJob(job));
        dagNodeStorage.updateDagStates(DagStates.RUNNING);
        failJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public List<DagBriefInfo> getDagList() {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, "", "");
        List<String> allDags = dagNodeStorage.getAllDags();
        List<DagBriefInfo> resList = Lists.newArrayList();
        allDags.forEach(name -> resList.add(new DagBriefInfo(name)));
        return resList;
    }

    @Override
    public List<DagBriefInfo> getDagJobDependencies(final String dagName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, "");
        Map<String, Set<String>> allDagConfigJobs = dagNodeStorage.getAllDagConfigJobs();
        List<DagBriefInfo> resList = Lists.newArrayList();
        allDagConfigJobs.forEach((key, value) -> {
            resList.add(new DagBriefInfo(dagName, key, Joiner.on(",").join(value)));
        });
        return resList;
    }

    private void triggerJobManual(final String job) {
        CuratorTransactionFinal curatorTransactionFinal = null;

        log.info("Trigger Dag job-{} manually.", job);
        try {
            JobNodePath jobNodePath = new JobNodePath(job);
            CuratorFramework rawClient = (CuratorFramework) regCenter.getRawClient();
            List<CuratorOp> opList = new ArrayList<>();
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                opList.add(rawClient.transactionOp().setData().forPath(jobNodePath.getInstanceNodePath(each), "TRIGGER".getBytes()));
            }
            rawClient.transaction().forOperations(opList);
            //CHECKSTYLE:OFF
        } catch (final Exception exp) {
            //CHECKSTYLE:ON
            log.error("Trigger Dag job-{} by hand in transaction Exception！", job, exp);
        }
    }

}
