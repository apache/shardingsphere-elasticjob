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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobDagConfiguration;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.exception.DagRuntimeException;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.state.JobStateEnum;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.DagJobExecutionEvent;

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
public class DagService implements CuratorCacheListener {
    public static final String ROOT_JOB = "self";

    private static final String DAG_LATCH_PATH = "/daglatch/";

    private static final int DEFAULT_RETRY_INTERVAL = 30;

    private static final String RETRY_PATH = "/dagretry/%s/%s";

    private final DagNodeStorage dagNodeStorage;

    private final JobDagConfiguration jobDagConfig;

    private final String jobName;

    private final String dagName;

    private final InterProcessMutex mutex;

    private final CoordinatorRegistryCenter regCenter;

    private final JobEventBus jobEventBus;

    private CuratorCache jobStateCache;

    private DistributedDelayQueue<String> delayQueue;

    public DagService(final CoordinatorRegistryCenter regCenter, final String jobName, final JobEventBus jobEventBus, final JobDagConfiguration jobDagConfig) {
        this.jobDagConfig = jobDagConfig;
        this.regCenter = regCenter;
        this.jobName = jobName;
        this.dagNodeStorage = new DagNodeStorage(regCenter, jobDagConfig.getDagName(), jobName);
        this.dagName = jobDagConfig.getDagName();
        this.jobEventBus = jobEventBus;
        if (StringUtils.equals(jobDagConfig.getDagDependencies(), ROOT_JOB)) {
            this.mutex = new InterProcessMutex((CuratorFramework) regCenter.getRawClient(), DAG_LATCH_PATH + dagName);
        } else {
            this.mutex = null;
        }
        regDagConfig();
    }

    public DagService(final CoordinatorRegistryCenter regCenter, final String dagName, final DagNodeStorage dagNodeStorage) {
        this.regCenter = regCenter;
        this.dagName = dagName;
        this.dagNodeStorage = dagNodeStorage;
        this.jobName = "";
        this.jobDagConfig = null;
        this.mutex = null;
        this.jobStateCache = null;
        this.delayQueue = null;
        this.jobEventBus = null;
    }

    /**
     * Init delay queue for retry jobs.
     *
     * @return DistributedDelayQueue
     */
    private DistributedDelayQueue<String> initDelayQueue() {
        String retryPath = String.format(RETRY_PATH, dagName, jobName);
        DistributedDelayQueue<String> delayQueue = QueueBuilder.builder((CuratorFramework) regCenter.getRawClient(), new JobRetryTrigger(regCenter, dagName), new QueueSerializer<String>() {
            @Override
            public byte[] serialize(final String item) {
                try {
                    return item.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("Dag-{}[{}] Init delay queue exception.", dagName, jobName, e);
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
            log.info("Dag-{}[{}] start delay queue, path={}", dagName, jobName, retryPath);
            //CHECKSTYLE:OFF
        } catch (Exception e) {
            //CHECKSTYLE:ON
            log.error("Dag-{}[{}] start delay queue Exception, path={}", dagName, jobName, retryPath, e);
        }

        return delayQueue;
    }

    private void startJobStateListener() {
        jobStateCache.listenable().addListener(this);
        try {
            jobStateCache.start();
            postEvent(DagJobStates.REG.getValue(), "Job register success");
            //CHECKSTYLE:OFF
        } catch (Exception exp) {
            //CHECKSTYLE:ON
            log.error("Start dag-{} job-{} state path listener Exception.", dagName, jobName, exp);
            // ignore
            postEvent(DagJobStates.REG.getValue(), "Job register Error:" + exp.getMessage());
        }
        log.info("Dag-{} job-{} state path listener has started success.", dagName, jobName);
    }

    private void stopJobStateListener() {
        jobStateCache.close();
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
     * Get dag status.
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
    private void regDagConfig() {
        this.dagNodeStorage.persistDagConfig(genDependenciesString());
        this.delayQueue = initDelayQueue();
        this.jobStateCache = CuratorCache.build((CuratorFramework) regCenter.getRawClient(), this.dagNodeStorage.pathOfJobNodeState());
        this.startJobStateListener();
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
            log.info("Dag-{} states already RUNNING", dagName);
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
            log.debug("DAG '{}' sleep short time until DAG graph completed. {}", dagName, count);
            BlockUtils.sleep(300L);
            if (count > 200) {
                log.error("Dag-{} Wait a long time with Dag graph NOT complete", dagName);
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
            log.debug("Dag-{} acquire lock error!", dagName, exp);
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
            log.debug("Dag-{} release lock error!", dagName, exp);
        }
    }

    /**
     * Check dag has cycle.
     *
     * @param allDagNode dag config info.
     */
    private void checkCycle(final Map<String, Set<String>> allDagNode) {
        Map<String, Set<String>> cloneMap = Maps.newHashMap();
        allDagNode.forEach((key, value) -> cloneMap.put(key, Sets.newHashSet(value)));

        while (removeSelf(cloneMap)) {
            if (log.isDebugEnabled()) {
                log.debug("Dag-{} remove root job.", dagName);
            }
        }
        if (!cloneMap.isEmpty()) {
            log.error("Dag {} find cycle {}", dagName, cloneMap.keySet().size());
            printCycleNode(cloneMap);
            throw new DagRuntimeException("Dag job find cycles");
        }
        log.info("Dag {} checkCycle success", dagName);
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
        return dagName + IpUtils.getIp() + ManagementFactory.getRuntimeMXBean().getName().split("@")[0] + date;
    }

    /**
     * When dag job start run ,check it's dependencies jobs states.
     */
    public void checkJobDependenciesState() {
        DagJobStates currentJobRunStates = dagNodeStorage.getDagJobRunStates(jobName);
        if (currentJobRunStates == DagJobStates.SUCCESS || currentJobRunStates == DagJobStates.FAIL) {
            log.info("DAG- {} job- {} 's states is {},Can not run again!", jobDagConfig.getDagName(), jobName, currentJobRunStates);
            throw new DagRuntimeException("Dag job has been completed");
        }
        if (isDagRootJob()) {
            log.debug("DAG {} job {} is root,No deps.", jobDagConfig.getDagName(), jobName);
            return;
        }

        String[] deps = dagNodeStorage.getJobDenpendencies();
        for (String dep : deps) {
            if (StringUtils.equals(dep, "self")) {
                continue;
            }
            DagJobStates jobRunStates = dagNodeStorage.getDagJobRunStates(dep);
            if (jobRunStates != DagJobStates.SUCCESS && jobRunStates != DagJobStates.SKIP) {
                log.info("DAG- {} job- {} Dependens job- {} Not ready!", dagName, jobName, dep);
                throw new DagRuntimeException("Dag dependencies jobs not Ready");
            }
        }
    }

    private boolean retryJob() {
        // get zk config ,check can retry?
        JobDagConfiguration jobDagConfig = getJobDagConfig(false);
        int times = dagNodeStorage.getJobRetryTimes();

        if (jobDagConfig.getRetryTimes() < 1 || jobDagConfig.getRetryTimes() <= times) {
            log.debug("Dag-{}[{}] config retry times{}, current times {} , skip retry!", dagName, jobName, jobDagConfig.getRetryTimes(), times);
            if (jobDagConfig.isDagSkipWhenFail()) {
                log.info("Dag-{}[{}] fail, mark as SKIP!", dagName, jobName);
                dagNodeStorage.updateDagJobStates(JobStateEnum.SKIP);
            } else {
                dagNodeStorage.updateDagJobStates(JobStateEnum.FAIL);
            }
            return false;
        }

        // send to retry queue
        try {
            long interval = (jobDagConfig.getRetryInterval() <= 0 ? DEFAULT_RETRY_INTERVAL : jobDagConfig.getRetryInterval()) * 1000L;
            delayQueue.put(dagName + "||" + jobName, System.currentTimeMillis() + interval);
            //CHECKSTYLE:OFF
        } catch (Exception ex) {
            //CHECKSTYLE:ON
            log.error("Dag-{}[{}] retry job to Delay queue Exception!", dagName, jobName, ex);
            return false;
        }

        dagNodeStorage.updateJobRetryTimes(times + 1);
        log.info("Dag-{}[{}] Retry job to delay queue success, times-[{}]", dagName, jobName, times + 1);
        postEvent("retry", "Put to DQ");
        return true;
    }

    /**
     * Get JobDagConfig from local or zk.
     *
     * @param fromLocal From local or register center.
     * @return dag config.
     */
    private JobDagConfiguration getJobDagConfig(final boolean fromLocal) {
        if (fromLocal) {
            return jobDagConfig;
        }
        ConfigurationService configurationService = new ConfigurationService(this.regCenter, this.jobName);
        JobConfiguration jobConfiguration = configurationService.load(false);
        if (jobConfiguration == null || jobConfiguration.getJobDagConfiguration() == null) {
            return jobDagConfig;
        }
        return jobConfiguration.getJobDagConfiguration();
    }

    /**
     * There is no dag job running.
     *
     * @return true if no job running.
     */
    private boolean hasNoJobRunning() {
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
            if (s.removeIf(x -> successList.contains(x) || skipList.contains(x))) {
                s.add("self");
            }
        });

        successList.stream().forEach(j -> allDagRunJobs.remove(j));
        skipList.stream().forEach(j -> allDagRunJobs.remove(j));

        allDagRunJobs.entrySet().forEach(x -> {
            if (x.getValue().isEmpty() || (x.getValue().size() == 1 && x.getValue().contains("self"))) {
                nextList.add(x.getKey());
            }
        });

        nextList.removeAll(runningList);
        nextList.removeAll(failList);

        log.info("Dag-{} acquire next should Trigger jobs: {}", dagName, nextList);
        if (log.isDebugEnabled()) {
            log.debug("Dag-{} NextTrigger all job nodes size:{}", dagName, allDagRunJobs.size());
            allDagRunJobs.forEach((key, value) -> log.debug("Dag-{} acquire next should Trigger JOBS, Graph- {} = {}", dagName, key, Joiner.on(",").join(value)));
            log.debug("Dag-{} acquire next should Trigger JOBS, SUCC-[{}], FAIL-[{}], RUN-[{}] , SKIP-[{}] ,Will Trigger-[{}].", dagName, successList, failList, runningList, skipList, nextList);
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

        log.info("Dag-{}[Statistics] totalJob-{}, successJob-{}, failJob-{}, runningJob-{}, skipJob-{}", dagName, totalJob, succJob, failJob, runningJob, skipJob);
        if (log.isDebugEnabled()) {
            log.debug("Dag-{}[Statistics] SUCC-[{}], FAIL-[{}], RUNNING-[{}], SKIP-[{}]", dagName, successList, failList, runningList, skipList);
        }

        if ((succJob + skipJob) == totalJob) {
            dagStates = DagStates.SUCCESS;
        } else if (failJob > 0) {
            dagStates = DagStates.FAIL;
        } else {
            dagStates = DagStates.RUNNING;
        }

        postEvent(dagStates.getValue(), "Dag Complete");
        log.info("Dag-{}[Statistics] DAG run complete, Final State-[{}]!", dagName, dagStates);
        dagNodeStorage.updateDagStates(dagStates);
    }

    private void postEvent(final String state, final String message) {
        if (null != jobEventBus) {
            jobEventBus.post(new DagJobExecutionEvent(dagName, jobName, dagNodeStorage.currentDagBatchNo(), state, message));
        }
    }

    /**
     * zkpath: jobName/state listener.
     * @param type event type.
     * @param oldData old data
     * @param data new data.
     */
    @Override
    public void event(final Type type, final ChildData oldData, final ChildData data) {
        if (!(type == Type.NODE_CHANGED || type == Type.NODE_CREATED)) {
            return;
        }

        JobStateEnum jobState = JobStateEnum.of(new String(data.getData()));
        log.info("Dag-{}[{}] receive job state EVENT-{}", dagName, jobName, jobState);

        if (jobState == JobStateEnum.RUNNING || jobState == JobStateEnum.NONE) {
            log.debug("Dag-{}[{}] receive job state EVENT NOT last state-[{}], skip.", dagName, jobName, jobState);
            return;
        }

        // deal with retry when fail
        if (jobState == JobStateEnum.FAIL) {
            if (retryJob()) {
                log.info("Dag-{}[{}] Put FAIL job to DQ Success! Waiting Triggered!", dagName, jobName);
                return;
            }
        } else {
            dagNodeStorage.updateDagJobStates(jobState);
        }

        postEvent(jobState.getValue(), "Job Complete");

        DagStates dagState = getDagStates();
        if (dagState == DagStates.PAUSE) {
            log.info("Dag-{} current dag state is PAUSE, Do not trigger next jobs!", dagName);
            return;
        }

        // Acquire next should trigger jobs
        List<String> willTriggerJobs = nextShouldTriggerJob();

        // If their is none job should trigger and current running job list is empty , start statistics dag state
        if (willTriggerJobs.isEmpty()) {
            if (hasNoJobRunning()) {
                log.info("Dag-{}, No job running, Start statistics The DAG State.", dagName);
                statisticsDagState();
                return;
            }
            log.info("Dag-{}[{}] No trigger job, Wating for other running jobs.", dagName, jobName);
        } else {
            // Else register next trigger jobs.
            log.info("Dag-{}[{}] trigger job list [{}].", dagName, jobName, willTriggerJobs);
            willTriggerJobs.forEach(job -> {
                postEvent("trigger", "Trigger Job");
                dagNodeStorage.triggerJob(job);
            });
        }
    }
}
