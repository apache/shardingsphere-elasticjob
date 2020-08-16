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

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * Trigger retry dag job.
 *
 **/
@Slf4j
public class JobRetryTrigger implements QueueConsumer<String> {
    private final CoordinatorRegistryCenter regCenter;

    private final String dagName;

    public JobRetryTrigger(final CoordinatorRegistryCenter regCenter, final String dagName) {
        this.regCenter = regCenter;
        this.dagName = dagName;
    }

    @Override
    public void consumeMessage(final String message) throws Exception {
        // message format: dagName||jobName
        // trigger the job only when dag state is running
        if (StringUtils.isEmpty(message)) {
            log.info("Dag-{} Retry job Receive message is empty, return!", dagName);
            return;
        }

        List<String> strings = Splitter.on("||").splitToList(message);
        if (strings.size() != 2) {
            log.info("Dag-{} Retry job message format not right! {}", dagName, message);
            return;
        }

        String jobName = strings.get(1);
        log.info("Dag-{} start Retry job-{}", dagName, jobName);
        DagNodeStorage dagNodeStorage = new DagNodeStorage(regCenter, dagName, jobName);

        DagStates dagState = DagStates.of(dagNodeStorage.currentDagStates());
        if (dagState != DagStates.RUNNING) {
            log.info("Dag-{} retry job, dag state-{} not RUNNING, quit!", dagName, jobName, dagState);
            return;
        }

        dagNodeStorage.triggerRetryJob();
    }

    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState newState) {

    }

}
