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

package org.apache.shardingsphere.elasticjob.lite.dag;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.DagRuntimeException;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.state.JobStateEnum;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.DagJobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.util.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Job dag service.
 */
@Slf4j
public class DagService implements PathChildrenCacheListener {
    public static final String ROOT_JOB = "self";

    private static final String DAG_LATCH_PATH = "/daglatch/";

    private static final int DEFAULT_RETRY_INTERVAL = 30;

    private static final String RETRY_PATH = "/dagretry/%s/%s";

    private final DagNodeStorage dagNodeStorage;

    private final JobDagConfig jobDagConfig;

    private final String jobName;

    private final String groupName;

    private final InterProcessMutex mutex;

    private final CoordinatorRegistryCenter regCenter;

    private final JobEventBus jobEventBus;

    private PathChildrenCache jobStatePathCache;

    private DistributedDelayQueue<String> delayQueue;

    public DagService(final CoordinatorRegistryCenter regCenter, final String jobName, final JobEventBus jobEventBus, final JobDagConfig jobDagConfig) {
        this.jobDagConfig = jobDagConfig;
        this.regCenter = regCenter;
        this.jobName = jobName;
        this.dagNodeStorage = new DagNodeStorage(regCenter, jobDagConfig.getDagGroup(), jobName);
        this.groupName = jobDagConfig.getDagGroup();
        this.jobEventBus = jobEventBus;
        if (StringUtils.equals(jobDagConfig.getDagDependencies(), ROOT_JOB)) {
            this.mutex = new InterProcessMutex((CuratorFramework) regCenter.getRawClient(), DAG_LATCH_PATH + groupName);
        } else {
            this.mutex = null;
        }
    }

    public DagService(final CoordinatorRegistryCenter regCenter, final String groupName, final DagNodeStorage dagNodeStorage) {
        this.regCenter = regCenter;
        this.groupName = groupName;
        this.dagNodeStorage = dagNodeStorage;
        this.jobName = "";
        this.jobDagConfig = null;
        this.mutex = null;
        this.jobStatePathCache = null;
        this.delayQueue = null;
        this.jobEventBus = null;
    }

    /**
     * Init delay queue for retry jobs.
     *
     * @return DistributedDelayQueue
     */
    private DistributedDelayQueue<String> initDelayQueue() {
        String retryPath = String.format(RETRY_PATH, groupName, jobName);
        DistributedDelayQueue<String> delayQueue = QueueBuilder.builder((CuratorFramework) regCenter.getRawClient(), new JobRetryTrigger(regCenter, groupName), new QueueSerializer<String>() {
            @Override
            public byte[] serialize(final String item) {
                try {
                    return item.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("Dag-{}[{}] Init delay queue exception.", groupName, jobName, e);
                }
                return null;
            }

            @Override
            public String deserialize(final byte[] bytes) {
                return new String(bytes);
            }
        }, retryPath).buildDelayQueue();

        try {
            delayQueue.start();
            log.info("Dag-{}[{}] start delay queue, path={}", groupName, jobName, retryPath);
            //CHECKSTYLE:OFF
        } catch (Exception e) {
            //CHECKSTYLE:ON
            log.error("Dag-{}[{}] start delay queue Exception, path={}", groupName, jobName, retryPath, e);
        }

        return delayQueue;
    }

    private void startJobStatePathListener() {
        jobStatePathCache.getListenable().addListener(this);
        try {
            jobStatePathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            postEvent(DagJobStates.REG.getValue(), "Job register success");
            //CHECKSTYLE:OFF
        } catch (Exception exp) {
            //CHECKSTYLE:ON
            log.error("Start dag-{} job-{} state path listener Exception.", groupName, jobName, exp);
            // ignore
            postEvent(DagJobStates.REG.getValue(), "Job register Error:" + exp.getMessage());
        }
        log.info("Dag-{} job-{} state path listener has started success.", groupName, jobName);
    }

    private void stopJobStatePathListener() {
        try {
            jobStatePathCache.close();
        } catch (IOException exp) {
            log.error("Stop dag-{} job-{} state path listener Exception.", groupName, jobName, exp);
        }
    }

    /**
     * Is dag root job.
     *
     * @return boolean is dag root job
     */
    public boolean isDagRootJob() {
        return StringUtils.equals(jobDagConfig.getDagDependencies(), "self");
    }

    /**
     * current dag status.
     *
     * @return DagStates
     */
    public DagStates getDagStates() {
        return DagStates.of(this.dagNodeStorage.currentDagStates());
    }

    /**
     * Persist Dag config into zk.
     * always overwrite.
     */
    public void regDagConfig() {
        this.dagNodeStorage.persistDagConfig(genDependenciesString());
        this.delayQueue = initDelayQueue();
        this.jobStatePathCache = new PathChildrenCache((CuratorFramework) regCenter.getRawClient(), this.dagNodeStorage.pathOfJobNodeState(), true);
        this.startJobStatePathListener();
    }

    private String genDependenciesString() {
        return jobDagConfig.getDagDependencies();
    }

    /**
     * 1. select leader ;
     * 2. ReGraph DAG ;
     * 3. Change DAG states to running
     */
    public void changeDagStatesAndReGraph() {
        if (null == mutex) {
            log.error("Need root job when change dag states and regraph!");
            throw new DagRuntimeException("Need root job when change dag states and regraph!");
        }

        if (!acquireDagLeader()) {
            blockUntilCompleted();
            return;
        }

        if (getDagStates() == DagStates.RUNNING) {
            log.info("Dag-{} states already RUNNING", groupName);
            return;
        }

        try {
            String batchNo = getBatchNo();
            Map<String, Set<String>> allDagNode = dagNodeStorage.getAllDagConfigJobs();
            checkCycle(allDagNode);
            dagNodeStorage.initDagGraph(allDagNode, batchNo);
            dagNodeStorage.updateDagStates(DagStates.RUNNING);
            dagNodeStorage.updateDagJobStates(JobStateEnum.RUNNING);
            // create graph event
            postEvent(DagJobStates.INIT.getValue(), "Create graph success");
            //CHECKSTYLE:OFF
        } catch (Exception ex) {
            //CHECKSTYLE:ON
            postEvent(DagJobStates.INIT.getValue(), "Create graph error:" + ex.getMessage());
        } finally {
            releaseDagLeader();
        }
    }

    private void blockUntilCompleted() {
        int count = 0;
        while (getDagStates() != DagStates.RUNNING) {
            count++;
            log.debug("DAG '{}' sleep short time until DAG graph completed. {}", groupName, count);
            BlockUtils.sleep(300L);
            if (count > 200) {
                log.error("Dag-{} Wait a long time with Dag graph NOT complete", groupName);
                throw new DagRuntimeException("Dag graph not complete!");
            }
        }
    }

    private boolean acquireDagLeader() {
        try {
            return mutex.acquire(200, TimeUnit.MILLISECONDS);
            //CHECKSTYLE:OFF
        } catch (Exception exp) {
            //CHECKSTYLE:ON
            log.debug("Dag-{} acquire lock error!", groupName, exp);
        }
        return false;
    }

    private void releaseDagLeader() {
        try {
            if (mutex.isAcquiredInThisProcess()) {
                mutex.release();
            }
            //CHECKSTYLE:OFF
        } catch (Exception exp) {
            //CHECKSTYLE:ON
            log.debug("Dag-{} release lock error!", groupName, exp);
        }
    }

    /**
     * Check dag has cycle.
     *
     * @param allDagNode dag config info.
     */
    public void checkCycle(final Map<String, Set<String>> allDagNode) {
        Map<String, Set<String>> cloneMap = Maps.newHashMap();
        allDagNode.forEach((key, value) -> cloneMap.put(key, Sets.newHashSet(value)));

        while (removeSelf(cloneMap)) {
            if (log.isDebugEnabled()) {
                log.debug("Dag-{} remove root job.", groupName);
            }
        }
        if (!cloneMap.isEmpty()) {
            log.error("Dag {} find cycle {}", groupName, cloneMap.keySet().size());
            printCycleNode(cloneMap);
            throw new DagRuntimeException("Dag job find cycles");
        }
        log.info("Dag {} checkCycle success", groupName);
    }

    private void printCycleNode(final Map<String, Set<String>> cloneMap) {
        cloneMap.forEach((k, v) -> {
            log.error("{} has cycle with {}", k, Joiner.on("|").join(v));
        });
    }

    private boolean removeSelf(final Map<String, Set<String>> cloneMap) {
        Iterator<Map.Entry<String, Set<String>>> iterator = cloneMap.entrySet().iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> next = iterator.next();
            Set<String> value = next.getValue();
            value.remove("self");
            if (value.isEmpty()) {
                markKeyAsSelf(cloneMap, next.getKey());
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    private void markKeyAsSelf(final Map<String, Set<String>> cloneMap, final String key) {
        cloneMap.values().forEach(s -> s.remove(key));
    }

    private String getBatchNo() {
        String date = DateFormatUtils.format(new Date(), "yyMMddHHmmss");
        return groupName + IpUtils.getIp() + ManagementFactory.getRuntimeMXBean().getName().split("@")[0] + date;
    }

    /**
     * When dag job start run ,check it's dependencies jobs states.
     */
    public void checkJobDependenciesState() {
        // 检查当前job 已经是终态不允许再次执行
        DagJobStates currentJobRunStates = dagNodeStorage.getDagJobRunStates(jobName);
        if (currentJobRunStates == DagJobStates.SUCCESS || currentJobRunStates == DagJobStates.FAIL) {
            log.info("DAG- {} job- {} 's states is {},Can not run again!", jobDagConfig.getDagGroup(), jobName, currentJobRunStates);
            throw new DagRuntimeException("Dag job has been completed");
        }
        if (isDagRootJob()) {
            log.debug("DAG {} job {} is root,No deps.", jobDagConfig.getDagGroup(), jobName);
            return;
        }

        // 要求dep skip 或 success
        String[] deps = dagNodeStorage.getJobDenpendencies();
        for (String dep : deps) {
            if (StringUtils.equals(dep, "self")) {
                continue;
            }
            DagJobStates jobRunStates = dagNodeStorage.getDagJobRunStates(dep);
            if (jobRunStates != DagJobStates.SUCCESS && jobRunStates != DagJobStates.SKIP) {
                log.info("DAG- {} job- {} Dependens job- {} Not ready!", groupName, jobName, dep);
                throw new DagRuntimeException("Dag dependencies jobs not Ready");
            }
        }
    }

    /**
     * zkpath: jobName/state listener.
     *
     * @param curatorFramework curator client
     * @param event path child event
     * @throws Exception exception
     */
    @Override
    public void childEvent(final CuratorFramework curatorFramework, final PathChildrenCacheEvent event) throws Exception {
        if (!(event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED
                || event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED)) {
            return;
        }

        JobStateEnum jobState = JobStateEnum.of(new String(event.getData().getData()));
        log.info("Dag-{}[{}] receive job state EVENT-{}", groupName, jobName, jobState);

        if (jobState == JobStateEnum.RUNNING || jobState == JobStateEnum.NONE) {
            log.debug("Dag-{}[{}] receive job state EVENT NOT last state-[{}], skip.", groupName, jobName, jobState);
            return;
        }

        // deal with retry when fail
        if (jobState == JobStateEnum.FAIL) {
            if (retryJob()) {
                log.info("Dag-{}[{}] Put FAIL job to DQ Success! Waiting Triggered!", groupName, jobName);
                return;
            }
        } else {
            dagNodeStorage.updateDagJobStates(jobState);
        }

        postEvent(jobState.getValue(), "Job Complete");

        DagStates dagState = getDagStates();
        if (dagState == DagStates.PAUSE) {
            log.info("Dag-{} current dag state is PAUSE, Do not trigger next jobs!", groupName);
            return;
        }

        // Acquire next should trigger jobs
        List<String> willTriggerJobs = nextShouldTriggerJob();

        // If their is none job should trigger and current running job list is empty , start statistics dag state
        if (willTriggerJobs.isEmpty()) {
            if (hasNoJobRunning()) {
                log.info("Dag-{}, No job running, Start statistics The DAG State.", groupName);
                statisticsDagState();
                return;
            }
            log.info("Dag-{}[{}] No trigger job, Wating for other running jobs.", groupName, jobName);
        } else {
            // Else register next trigger jobs.
            log.info("Dag-{}[{}] trigger job list [{}].", groupName, jobName, willTriggerJobs);
            willTriggerJobs.forEach(job -> {
                postEvent("trigger", "Trigger Job");
                dagNodeStorage.triggerJob(job);
            });
        }
    }

    private boolean retryJob() {
        // get zk config ,check can retry?
        JobDagConfig jobDagConfig = getJobDagConfig(false);
        int times = dagNodeStorage.getJobRetryTimes();

        if (jobDagConfig.getRetryTimes() < 1 || jobDagConfig.getRetryTimes() <= times) {
            log.debug("Dag-{}[{}] config retry times{}, current times {} , skip retry!", groupName, jobName, jobDagConfig.getRetryTimes(), times);
            if (jobDagConfig.isDagSkipWhenFail()) {
                log.info("Dag-{}[{}] fail, mark as SKIP!", groupName, jobName);
                dagNodeStorage.updateDagJobStates(JobStateEnum.SKIP);
            } else {
                dagNodeStorage.updateDagJobStates(JobStateEnum.FAIL);
            }
            return false;
        }

        // send to retry queue
        try {
            long interval = (jobDagConfig.getRetryInterval() <= 0 ? DEFAULT_RETRY_INTERVAL : jobDagConfig.getRetryInterval()) * 1000L;
            delayQueue.put(groupName + "||" + jobName, System.currentTimeMillis() + interval);
            //CHECKSTYLE:OFF
        } catch (Exception exp) {
            //CHECKSTYLE:ON
            log.error("Dag-{}[{}] retry job to Delay queue Exception!", groupName, jobName, exp);
            return false;
        }

        dagNodeStorage.updateJobRetryTimes(times + 1);
        log.info("Dag-{}[{}] Retry job to delay queue success, times-[{}]", groupName, jobName, times + 1);
        postEvent("retry", "Put to DQ");
        return true;
    }

    /**
     * Get JobDagConfig from local or zk.
     *
     * @param fromLocal From local or register center.
     * @return dag config.
     */
    private JobDagConfig getJobDagConfig(final boolean fromLocal) {
        if (fromLocal) {
            return jobDagConfig;
        }
        ConfigurationService configurationService = new ConfigurationService(this.regCenter, this.jobName);
        JobConfiguration jobConfiguration = configurationService.load(false);
        if (jobConfiguration == null || jobConfiguration.getJobDagConfig() == null) {
            return jobDagConfig;
        }
        return jobConfiguration.getJobDagConfig();
    }

    /**
     * There is no dag job running.
     *
     * @return true if no job running.
     */
    public boolean hasNoJobRunning() {
        return dagNodeStorage.getDagJobListByState(DagJobStates.RUNNING).isEmpty();
    }

    /**
     * Acquire next should trigger jobs.
     *
     * @return next should trigger jobs
     */
    public List<String> nextShouldTriggerJob() {
        final Map<String, Set<String>> allDagRunJobs = dagNodeStorage.getAllDagGraphJobs();
        final List<String> successList = dagNodeStorage.getDagJobListByState(DagJobStates.SUCCESS);
        final List<String> failList = dagNodeStorage.getDagJobListByState(DagJobStates.FAIL);
        final List<String> runningList = dagNodeStorage.getDagJobListByState(DagJobStates.RUNNING);
        final List<String> skipList = dagNodeStorage.getDagJobListByState(DagJobStates.SKIP);
        List<String> nextList = Lists.newLinkedList();

        allDagRunJobs.values().forEach(s -> {
            if (s.removeIf(x -> successList.contains(x))) {
                s.add("self");
            }
        });

        successList.stream().forEach(j -> allDagRunJobs.remove(j));

        allDagRunJobs.entrySet().forEach(x -> {
            if (x.getValue().isEmpty() || (x.getValue().size() == 1 && x.getValue().contains("self"))) {
                nextList.add(x.getKey());
            }
        });

        nextList.removeAll(runningList);
        nextList.removeAll(failList);
        nextList.remove(skipList);

        log.info("Dag-{} acquire next should Trigger jobs: {}", groupName, nextList);
        if (log.isDebugEnabled()) {
            log.debug("Dag-{} NextTrigger all job nodes size:{}", groupName, allDagRunJobs.size());
            allDagRunJobs.forEach((key, value) -> log.debug("Dag-{} acquire next should Trigger JOBS, Graph- {} = {}", groupName, key, Joiner.on(",").join(value)));
            log.debug("Dag-{} acquire next should Trigger JOBS, SUCC-[{}], FAIL-[{}], RUN-[{}] , SKIP-[{}] ,Will Trigger-[{}].", groupName, successList, failList, runningList, skipList, nextList);
        }

        return nextList;
    }

    /**
     * When Dag jobs all completed, Statistics dag state.
     *
     */
    private void statisticsDagState() {
        DagStates dagStates = null;
        Map<String, Set<String>> allDagRunJobs = dagNodeStorage.getAllDagGraphJobs();
        List<String> successList = dagNodeStorage.getDagJobListByState(DagJobStates.SUCCESS);
        List<String> runningList = dagNodeStorage.getDagJobListByState(DagJobStates.RUNNING);
        List<String> failList = dagNodeStorage.getDagJobListByState(DagJobStates.FAIL);
        List<String> skipList = dagNodeStorage.getDagJobListByState(DagJobStates.SKIP);
        int totalJob = allDagRunJobs.size();
        int succJob = successList.size();
        int failJob = failList.size();
        int skipJob = skipList.size();
        int runningJob = runningList.size();

        log.info("Dag-{}[Statistics] totalJob-{}, successJob-{}, failJob-{}, runningJob-{}, skipJob-{}", groupName, totalJob, succJob, failJob, runningJob, skipJob);
        if (log.isDebugEnabled()) {
            log.debug("Dag-{}[Statistics] SUCC-[{}], FAIL-[{}], RUNNING-[{}], SKIP-[{}]", groupName, successList, failList, runningList, skipList);
        }

        if ((succJob + skipJob) == totalJob) {
            dagStates = DagStates.SUCCESS;
        } else if (failJob > 0) {
            dagStates = DagStates.FAIL;
        } else {
            dagStates = DagStates.RUNNING;
        }

        postEvent(dagStates.getValue(), "Dag Complete");
        log.info("Dag-{}[Statistics] DAG run complete, Final State-[{}]!", groupName, dagStates);
        dagNodeStorage.updateDagStates(dagStates);
    }

    private void postEvent(final String state, final String message) {
        if (null != jobEventBus) {
            jobEventBus.post(new DagJobExecutionEvent(groupName, jobName, dagNodeStorage.currentDagBatchNo(), state, message));
        }
    }

}
