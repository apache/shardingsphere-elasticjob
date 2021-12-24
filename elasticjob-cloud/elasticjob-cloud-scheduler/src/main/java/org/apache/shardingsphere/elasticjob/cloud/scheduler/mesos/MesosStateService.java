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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.util.HttpClientUtils;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Mesos state service.
 */
@Slf4j
public class MesosStateService {
    
    private static String stateUrl;
    
    private final FrameworkIDService frameworkIDService;
    
    public MesosStateService(final CoordinatorRegistryCenter regCenter) {
        frameworkIDService = new FrameworkIDService(regCenter);
    }
    
    /**
     * Register master info of Mesos.
     *
     * @param hostName hostname of master
     * @param port port of master
     */
    public static synchronized void register(final String hostName, final int port) {
        stateUrl = String.format("http://%s:%d/state", hostName, port);
    }
    
    /**
     * Deregister master info of Mesos.
     */
    public static synchronized void deregister() {
        stateUrl = null;
    }
    
    /**
     * Get sandbox info.
     *
     * @param appName app name
     * @return sandbox info in json format
     * @throws JsonParseException parse json exception
     */
    public Collection<Map<String, String>> sandbox(final String appName) throws JsonParseException {
        JsonObject state = fetch(stateUrl);
        List<Map<String, String>> result = new ArrayList<>();
        for (JsonObject each : findExecutors(state.getAsJsonArray("frameworks"), appName)) {
            JsonArray slaves = state.get("slaves").getAsJsonArray();
            String slaveHost = null;
            for (int i = 0; i < slaves.size(); i++) {
                JsonObject slave = slaves.get(i).getAsJsonObject();
                if (each.get("slave_id").getAsString().equals(slave.get("id").getAsString())) {
                    slaveHost = slave.get("pid").getAsString().split("@")[1];
                }
            }
            Preconditions.checkNotNull(slaveHost);
            JsonObject slaveState = fetch(String.format("http://%s/state", slaveHost));
            String workDir = slaveState.get("flags").getAsJsonObject().get("work_dir").getAsString();
            Collection<JsonObject> executorsOnSlave = findExecutors(slaveState.get("frameworks").getAsJsonArray(), appName);
            for (JsonObject executorOnSlave : executorsOnSlave) {
                Map<String, String> r = new LinkedHashMap<>();
                r.put("hostname", slaveState.get("hostname").getAsString());
                r.put("path", executorOnSlave.get("directory").getAsString().replace(workDir, ""));
                result.add(r);
            }
        }
        return result;
    }
    
    /**
     * Get executor by app name.
     *
     * @param appName app name
     * @return executor state info
     * @throws JsonParseException parse json exception
     */
    public Collection<ExecutorStateInfo> executors(final String appName) throws JsonParseException {
        return findExecutors(fetch(stateUrl).get("frameworks").getAsJsonArray(), appName).stream().map(each -> {
            try {
                return ExecutorStateInfo.builder().id(getExecutorId(each)).slaveId(each.get("slave_id").getAsString()).build();
            } catch (final JsonParseException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Get all executors.
     *
     * @return collection of executor state info
     * @throws JsonParseException parse json exception
     */
    public Collection<ExecutorStateInfo> executors() throws JsonParseException {
        return executors(null);
    }
    
    private JsonObject fetch(final String url) {
        Preconditions.checkState(!Strings.isNullOrEmpty(url));
        return GsonFactory.getJsonParser().parse(HttpClientUtils.httpGet(url).getContent()).getAsJsonObject();
    }
    
    private Collection<JsonObject> findExecutors(final JsonArray frameworks, final String appName) throws JsonParseException {
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        String frameworkID;
        if (frameworkIDOptional.isPresent()) {
            frameworkID = frameworkIDOptional.get();
        } else {
            return Collections.emptyList();
        }
        List<JsonObject> result = new LinkedList<>();
        for (int i = 0; i < frameworks.size(); i++) {
            JsonObject framework = frameworks.get(i).getAsJsonObject();
            if (!framework.get("id").getAsString().equals(frameworkID)) {
                continue;
            }
            JsonArray executors = framework.get("executors").getAsJsonArray();
            for (int j = 0; j < executors.size(); j++) {
                JsonObject executor = executors.get(j).getAsJsonObject();
                if (null == appName || appName.equals(getExecutorId(executor).split("@-@")[0])) {
                    result.add(executor);
                }
            }
        }
        return result;
    }
    
    private String getExecutorId(final JsonObject executor) throws JsonParseException {
        return executor.has("id") ? executor.get("id").getAsString() : executor.get("executor_id").getAsString();
    }
    
    @Builder
    @Getter
    public static final class ExecutorStateInfo {
        
        private final String id;
        
        private final String slaveId;
    }
}
