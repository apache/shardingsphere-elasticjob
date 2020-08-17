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

package org.apache.shardingsphere.elasticjob.lite.internal.dag;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.shardingsphere.elasticjob.lite.internal.state.JobStateEnum;
import org.apache.shardingsphere.elasticjob.lite.internal.state.JobStateNode;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dag storage class.
 *             DAG  zk path:
 *             /dag/
 *                 {dagName}/
 *                             config/ {jobName} value=dependencies comm split
 *                             graph/ {jobName} value=dependencies comm split
 *                             states  value= dag states
 *                             running/ {jobName}
 *                             success/ {jobName}
 *                             fail/ {jobName}
 *                             skip/ {jobName}
 *                             retry/ {jobName}
 */
@Slf4j
public final class DagNodeStorage {

    private static final String DAG_ROOT = "/dag/%s";

    private static final String DAG_CONFIG = "/dag/%s/config";

    private static final String DAG_CONFIG_JOB = "/dag/%s/config/%s";

    private static final String DAG_STATES = "/dag/%s/states";

    private static final String DAG_GRAPH = "/dag/%s/graph";

    private static final String DAG_GRAPH_JOB = "/dag/%s/graph/%s";

    private static final String DAG_GRAPH_JOB_RETRYTIMES = "/dag/%s/graph/%s/retry";

    private static final String DAG_RUNNING = "/dag/%s/running";

    private static final String DAG_RUNNING_JOB = "/dag/%s/running/%s";

    private static final String DAG_SUCCESS = "/dag/%s/success";

    private static final String DAG_SUCCESS_JOB = "/dag/%s/success/%s";

    private static final String DAG_FAIL = "/dag/%s/fail";

    private static final String DAG_FAIL_JOB = "/dag/%s/fail/%s";

    private static final String DAG_SKIP = "/dag/%s/skip";

    private static final String DAG_SKIP_JOB = "/dag/%s/skip/%s";

    private static final String DAG_RETRY = "/dag/%s/retry";

    private static final String DAG_RETRY_JOB = "/dag/%s/retry/%s";

    private final CoordinatorRegistryCenter regCenter;

    private final String jobName;

    private final String dagName;

    /**
     * Constructor.
     *
     * @param regCenter register center
     * @param dagName dag name
     * @param jobName dag job name
     */
    public DagNodeStorage(final CoordinatorRegistryCenter regCenter, final String dagName, final String jobName) {
        this.regCenter = regCenter;
        this.jobName = jobName;
        this.dagName = dagName;
    }

    /**
     * Persist /dag/dagName/config/jobName ,value=job's dependencies with comm split.
     *
     * @param value job dependencies.
     */
    public void persistDagConfig(final String value) {
        regCenter.persist(pathOfDagConfigJob(jobName), value);
    }

    private String pathOfDagRoot() {
        return String.format(DAG_ROOT, dagName);
    }

    private String pathOfDagConfig() {
        return String.format(DAG_CONFIG, dagName);
    }

    private String pathOfDagConfigJob(final String jobName) {
        return String.format(DAG_CONFIG_JOB, dagName, jobName);
    }

    private String pathOfDagStates() {
        return String.format(DAG_STATES, dagName);
    }

    private String pathOfDagGraph() {
        return String.format(DAG_GRAPH, dagName);
    }

    private String pathOfDagGraphJob(final String jobName) {
        return String.format(DAG_GRAPH_JOB, dagName, jobName);
    }

    private String pathOfDagGraphJobRetryTimes() {
        return String.format(DAG_GRAPH_JOB_RETRYTIMES, dagName, jobName);
    }

    private String pathOfDagRunning() {
        return String.format(DAG_RUNNING, dagName);
    }

    private String pathOfDagRunningJob(final String jobName) {
        return String.format(DAG_RUNNING_JOB, dagName, jobName);
    }

    private String pathOfDagSuccess() {
        return String.format(DAG_SUCCESS, dagName);
    }

    private String pathOfDagSuccessJob(final String jobName) {
        return String.format(DAG_SUCCESS_JOB, dagName, jobName);
    }

    private String pathOfDagFail() {
        return String.format(DAG_FAIL, dagName);
    }

    private String pathOfDagFailJob(final String jobName) {
        return String.format(DAG_FAIL_JOB, dagName, jobName);
    }

    private String pathOfDagSkip() {
        return String.format(DAG_SKIP, dagName);
    }

    private String pathOfDagSkipJob(final String jobName) {
        return String.format(DAG_SKIP_JOB, dagName, jobName);
    }

    private String pathOfDagRetry() {
        return String.format(DAG_RETRY, dagName);
    }

    private String pathOfDagRetryJob(final String jobName) {
        return String.format(DAG_RETRY_JOB, dagName, jobName);
    }

    /**
     * job state path.
     *
     * @return path of job state.
     */
    public String pathOfJobNodeState() {
        return String.format("/%s/%s", jobName, JobStateNode.ROOT_STATE);
    }

    /**
     * Init dag graph before run.
     *
     * @param allDagNode dag job name and dependencies.
     * @param batchNo batch no
     */
    public void initDagGraph(final Map<String, Set<String>> allDagNode, final String batchNo) {
        log.debug("Dag-{}, before create Dag Graph, clean exist path.", dagName);
        // clean
        if (regCenter.isExisted(pathOfDagGraph())) {
            regCenter.remove(pathOfDagGraph());
        }
        if (regCenter.isExisted(pathOfDagStates())) {
            regCenter.remove(pathOfDagStates());
        }
        if (regCenter.isExisted(pathOfDagRunning())) {
            regCenter.remove(pathOfDagRunning());
        }
        if (regCenter.isExisted(pathOfDagSuccess())) {
            regCenter.remove(pathOfDagSuccess());
        }
        if (regCenter.isExisted(pathOfDagFail())) {
            regCenter.remove(pathOfDagFail());
        }
        if (regCenter.isExisted(pathOfDagSkip())) {
            regCenter.remove(pathOfDagSkip());
        }
        if (regCenter.isExisted(pathOfDagRetry())) {
            regCenter.remove(pathOfDagRetry());
        }

        log.debug("Dag-{}, Create Dag Graph, create path.", dagName);
        // create path
        regCenter.persist(pathOfDagGraph(), batchNo);
        regCenter.persist(pathOfDagStates(), "");
        regCenter.persist(pathOfDagRunning(), "");
        regCenter.persist(pathOfDagSuccess(), "");
        regCenter.persist(pathOfDagFail(), "");
        regCenter.persist(pathOfDagSkip(), "");
        regCenter.persist(pathOfDagRetry(), "");

        log.debug("Dag-{}, Create Dag Graph, create graph.", dagName);
        // init graph
        for (Map.Entry<String, Set<String>> entry : allDagNode.entrySet()) {
            regCenter.persist(pathOfDagGraphJob(entry.getKey()), Joiner.on(",").join(entry.getValue()));
        }

        log.info("Dag-{}, Create Dag Graph success.", dagName);
    }

    /**
     * Get value of /dag/dagName/graph.
     *
     * @return batch no.
     */
    public String currentDagBatchNo() {
        return regCenter.getDirectly(pathOfDagGraph());
    }

    /**
     * get value of /dag/dagName/states.
     *
     * @return dag state
     */
    public String currentDagStates() {
        if (this.regCenter.isExisted(pathOfDagStates())) {
            return this.regCenter.getDirectly(pathOfDagStates());
        }
        return "";
    }

    /**
     * update value of /dag/dagName/states.
     *
     * @param dagStates dag state
     */
    public void updateDagStates(final DagStates dagStates) {
        regCenter.update(pathOfDagStates(), dagStates.getValue());
    }


    /**
     * running:/dag/dagName/running/{}.
     * success:/dag/dagName/success/{}.
     * fail:/dag/dagName/fail/{}.
     *
     * @param jobState job state
     */
    public void updateDagJobStates(final JobStateEnum jobState) {
        if (jobState == JobStateEnum.RUNNING) {
            regCenter.persist(pathOfDagRunningJob(jobName), String.valueOf(System.currentTimeMillis()));
            return;
        }
        regCenter.remove(pathOfDagRunningJob(jobName));
        if (jobState == JobStateEnum.SUCCESS) {
            regCenter.persist(pathOfDagSuccessJob(jobName), String.valueOf(System.currentTimeMillis()));
        } else if (jobState == JobStateEnum.FAIL) {
            regCenter.persist(pathOfDagFailJob(jobName), String.valueOf(System.currentTimeMillis()));
        } else if (jobState == JobStateEnum.SKIP) {
            regCenter.persist(pathOfDagSkipJob(jobName), String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * Get /dag/dagName/config/all config jobs with their dependencies.
     *
     * @return dag config info
     */
    public Map<String, Set<String>> getAllDagConfigJobs() {
        Map<String, Set<String>> map = Maps.newHashMap();
        List<String> childrenKeys = this.regCenter.getChildrenKeys(pathOfDagConfig());
        childrenKeys.forEach(s -> map.put(s, Sets.newHashSet(splitJobDeps(regCenter.getDirectly(pathOfDagConfigJob(s))))));
        return map;
    }

    /**
     * Get /dag/dagName/graph/all nodes with their dependencies.
     *
     * @return dag graph info
     */
    public Map<String, Set<String>> getAllDagGraphJobs() {
        List<String> jobList = this.regCenter.getChildrenKeys(pathOfDagGraph());
        Map<String, Set<String>> allDagMap = Maps.newHashMap();
        jobList.forEach(j ->
            allDagMap.put(j, Sets.newHashSet(splitJobDeps(regCenter.getDirectly(pathOfDagGraphJob(j))))));
        return allDagMap;
    }

    private Set<String> splitJobDeps(final String j) {
        return Splitter.on(",").trimResults().splitToList(j).stream().collect(Collectors.toSet());
    }

    /**
     * Get dag job lists by their state.
     *
     * @param dagJobState job state
     * @return job name lists
     */
    public List<String> getDagJobListByState(final DagJobStates dagJobState) {
        if (dagJobState == DagJobStates.RUNNING) {
            return regCenter.getChildrenKeys(pathOfDagRunning());
        }
        if (dagJobState == DagJobStates.SUCCESS) {
            return regCenter.getChildrenKeys(pathOfDagSuccess());
        }
        if (dagJobState == DagJobStates.FAIL) {
            return regCenter.getChildrenKeys(pathOfDagFail());
        }
        if (dagJobState == DagJobStates.SKIP) {
            return regCenter.getChildrenKeys(pathOfDagSkip());
        }
        if (dagJobState == DagJobStates.RETRY) {
            return regCenter.getChildrenKeys(pathOfDagRetry());
        }
        return Lists.newArrayList();
    }

    private CuratorFramework getClient() {
        return (CuratorFramework) regCenter.getRawClient();
    }

    /**
     * Get job denpendencies from reg center.
     *
     * @return job denpendencies.
     */
    public String[] getJobDenpendencies() {
        return StringUtils.split(this.regCenter.get(pathOfDagGraphJob(jobName)), ",");
    }

    /**
     * Get job state.
     *
     * @param depJob job name.
     * @return job state
     */
    public DagJobStates getDagJobRunStates(final String depJob) {
        if (regCenter.isExisted(pathOfDagSuccessJob(depJob))) {
            return DagJobStates.SUCCESS;
        }
        if (regCenter.isExisted(pathOfDagFailJob(depJob))) {
            return DagJobStates.FAIL;
        }
        if (regCenter.isExisted(pathOfDagSkipJob(depJob))) {
            return DagJobStates.SKIP;
        }
        if (regCenter.isExisted(pathOfDagRunningJob(depJob))) {
            return DagJobStates.RUNNING;
        }
        return DagJobStates.READY;
    }

    /**
     * Trigger the job.
     *
     * @param job job name
     */
    public void triggerJob(final String job) {
        if (isJobTriggered(job)) {
            log.debug("Dag-{} Job-{} has already been tiggered!", dagName, job);
            return;
        }
        log.debug("Dag-{} trigger job-[{}] in transaction.", dagName, job);
        try {
            CuratorFramework rawClient = (CuratorFramework) regCenter.getRawClient();
            List<CuratorOp> opList = new ArrayList<>();
            opList.add(rawClient.transactionOp().create().forPath(pathOfDagRunningJob(job)));
            JobNodePath jobNodePath = new JobNodePath(job);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                opList.add(rawClient.transactionOp().setData().forPath(jobNodePath.getInstanceNodePath(each), "TRIGGER".getBytes()));
            }
            rawClient.transaction().forOperations(opList);
            //CHECKSTYLE:OFF
        } catch (final Exception exp) {
            //CHECKSTYLE:ON
            log.debug("Dag-{}[{}] trigger job in transaction Exception！", dagName, job, exp);
        }

        log.info("Dag-{}[{}] has been triggered [{}]", dagName, job, isJobTriggered(job));
        printZkPath();
    }

    /**
     * Trigger retry job.
     */
    public void triggerRetryJob() {
        log.debug("Dag-{} trigger RETRY job-[{}] in transaction.", dagName, jobName);
        try {
            CuratorFramework rawClient = (CuratorFramework) regCenter.getRawClient();
            List<CuratorOp> opList = new ArrayList<>();
            opList.add(rawClient.transactionOp().delete().forPath(pathOfDagRetryJob(jobName)));
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath())) {
                opList.add(rawClient.transactionOp().setData().forPath(jobNodePath.getInstanceNodePath(each), "TRIGGER".getBytes()));
            }
            rawClient.transaction().forOperations(opList);
            //CHECKSTYLE:OFF
        } catch (final Exception exp) {
            //CHECKSTYLE:ON
            log.debug("Dag-{}[{}] trigger RETRY job in transaction Exception！", dagName, jobName, exp);
        }

        log.info("Dag-{}[{}] RETRY job has been triggered!", dagName, jobName);
        printZkPath();
    }

    private void printZkPath() {
        if (log.isDebugEnabled()) {
            log.debug("Dag-{}[{}] after trigger:", dagName, jobName);
            log.debug("Dag-{}[{}] ZK path of SUCC  : {}", dagName, jobName, getDagJobListByState(DagJobStates.SUCCESS));
            log.debug("Dag-{}[{}] ZK path of RUN   : {}", dagName, jobName, getDagJobListByState(DagJobStates.RUNNING));
            log.debug("Dag-{}[{}] ZK path of FAIL  : {}", dagName, jobName, getDagJobListByState(DagJobStates.FAIL));
            log.debug("Dag-{}[{}] ZK path of SKIP  : {}", dagName, jobName, getDagJobListByState(DagJobStates.SKIP));
            log.debug("Dag-{}[{}] ZK path of RETRY : {}", dagName, jobName, getDagJobListByState(DagJobStates.RETRY));
        }
    }

    private boolean isJobTriggered(final String job) {
        return regCenter.isExisted(pathOfDagRunningJob(job)) || regCenter.isExisted(pathOfDagSuccessJob(job)) || regCenter.isExisted(pathOfDagFailJob(job));
    }

    /**
     * Get current job retry times.
     *
     * @return retry times
     */
    public int getJobRetryTimes() {
        String times = regCenter.getDirectly(pathOfDagGraphJobRetryTimes());
        if (StringUtils.isEmpty(times)) {
            return 0;
        }
        return Integer.valueOf(times);
    }


    /**
     * Add dag retry job times.
     * Persist jobName to path '/dag/dagName/retry'
     *
     * @param retryTimes retry times.
     */
    public void updateJobRetryTimes(final int retryTimes) {
        regCenter.persist(pathOfDagRetryJob(jobName), "");
        regCenter.persist(pathOfDagGraphJobRetryTimes(), "" + retryTimes);
    }

    /**
     * Remove fail job from register center.
     *
     * @param job fail job name.
     */
    public void removeFailJob(final String job) {
        regCenter.remove(pathOfDagFailJob(job));
    }

    /**
     * Get dag list.
     *
     * @return list fo dag name
     */
    public List<String> getAllDags() {
        return regCenter.getChildrenKeys("/dag");
    }
}
