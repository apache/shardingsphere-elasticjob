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
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.shardingsphere.elasticjob.lite.dag.DagJobStates;
import org.apache.shardingsphere.elasticjob.lite.dag.DagNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.dag.DagService;
import org.apache.shardingsphere.elasticjob.lite.dag.DagStates;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.DagOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.DagBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

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
    public boolean toggleDagPause(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.RUNNING) {
            log.error("Current dag-{} not RUNNING , Cannot turn to PAUSE", groupName);
            return false;
        }
        dagNodeStorage.updateDagStates(DagStates.PAUSE);
        return true;
    }

    @Override
    public boolean toggleDagResume(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.PAUSE) {
            log.error("Current dag-{} not PAUSE , Cannot turn to RUNNING", groupName);
            return false;
        }

        // get next should trigger job.
        DagService dagService = new DagService(regCenter, groupName, dagNodeStorage);
        List<String> nextJobs = dagService.nextShouldTriggerJob();
        dagNodeStorage.updateDagStates(DagStates.RUNNING);
        nextJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public boolean toggleDagStart(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates == DagStates.RUNNING || dagStates == DagStates.PAUSE) {
            log.error("Can not start DAG-{}, current state-{}", groupName, dagStates);
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

        log.info("Dag-{} start jobs [{}]", groupName, rootJobs);
        rootJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public boolean toggleDagStop(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.PAUSE) {
            log.error("Current dag-{} not PAUSE , Cannot turn to FAIL", groupName);
            return false;
        }
        dagNodeStorage.updateDagStates(DagStates.FAIL);
        return true;
    }

    @Override
    public boolean toggleDagRerunWhenFail(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        DagStates dagStates = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagStates != DagStates.FAIL) {
            log.error("Current dag-{} not FAIL , Cannot rerun fail jobs", groupName);
            return false;
        }

        // delete fail job
        List<String> failJobs = dagNodeStorage.getDagJobListByState(DagJobStates.FAIL);
        if (failJobs.isEmpty()) {
            log.error("Dag-{} don't have fail jobs", groupName);
            return false;
        }
        log.info("Dag-{} rerun fail jobs [{}]", groupName, failJobs);
        failJobs.forEach(job -> dagNodeStorage.removeFailJob(job));
        dagNodeStorage.updateDagStates(DagStates.RUNNING);
        failJobs.forEach(job -> triggerJobManual(job));
        return true;
    }

    @Override
    public List<DagBriefInfo> getDagGroupNameList() {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, "", "");
        List<String> allDagGroups = dagNodeStorage.getAllDagGroups();
        List<DagBriefInfo> resList = Lists.newArrayList();
        allDagGroups.forEach(group -> resList.add(new DagBriefInfo(group)));
        return resList;
    }

    @Override
    public List<DagBriefInfo> getDagJobDependencies(final String groupName) {
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, groupName, "");
        Map<String, Set<String>> allDagConfigJobs = dagNodeStorage.getAllDagConfigJobs();
        List<DagBriefInfo> resList = Lists.newArrayList();
        allDagConfigJobs.forEach((key, value) -> {
            resList.add(new DagBriefInfo(groupName, key, Joiner.on(",").join(value)));
        });
        return resList;
    }

    private void triggerJobManual(final String job) {
        CuratorTransactionFinal curatorTransactionFinal = null;

        log.info("Trigger Dag job-{} manually.", job);
        try {
            JobNodePath jobNodePath = new JobNodePath(job);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                if (curatorTransactionFinal == null) {
                    curatorTransactionFinal = ((CuratorFramework) regCenter.getRawClient()).inTransaction().setData().forPath(jobNodePath.getInstanceNodePath(each), "TRIGGER".getBytes()).and();
                } else {
                    curatorTransactionFinal.setData().forPath(jobNodePath.getInstanceNodePath(each), "TRIGGER".getBytes()).and();
                }
            }
            curatorTransactionFinal.commit();
            //CHECKSTYLE:OFF
        } catch (final Exception exp) {
            //CHECKSTYLE:ON
            log.error("Trigger Dag job-{} by hand in transaction Exception！", job, exp);
        }
    }

}
