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
import com.sun.jersey.api.client.Client;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @throws JSONException parse json exception
     */
    public JsonArray sandbox(final String appName) throws JSONException {
        JSONObject state = fetch(stateUrl);
        JsonArray result = new JsonArray();
        for (JSONObject each : findExecutors(state.getJSONArray("frameworks"), appName)) {
            JSONArray slaves = state.getJSONArray("slaves");
            String slaveHost = null;
            for (int i = 0; i < slaves.length(); i++) {
                JSONObject slave = slaves.getJSONObject(i);
                if (each.getString("slave_id").equals(slave.getString("id"))) {
                    slaveHost = slave.getString("pid").split("@")[1];
                }
            }
            Preconditions.checkNotNull(slaveHost);
            JSONObject slaveState = fetch(String.format("http://%s/state", slaveHost));
            String workDir = slaveState.getJSONObject("flags").getString("work_dir");
            Collection<JSONObject> executorsOnSlave = findExecutors(slaveState.getJSONArray("frameworks"), appName);
            for (JSONObject executorOnSlave : executorsOnSlave) {
                JsonObject r = new JsonObject();
                r.addProperty("hostname", slaveState.getString("hostname"));
                r.addProperty("path", executorOnSlave.getString("directory").replace(workDir, ""));
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
     * @throws JSONException parse json exception
     */
    public Collection<ExecutorStateInfo> executors(final String appName) throws JSONException {
        return findExecutors(fetch(stateUrl).getJSONArray("frameworks"), appName).stream().map(each -> {
            try {
                return ExecutorStateInfo.builder().id(getExecutorId(each)).slaveId(each.getString("slave_id")).build();
            } catch (final JSONException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Get all executors.
     *
     * @return collection of executor state info
     * @throws JSONException parse json exception
     */
    public Collection<ExecutorStateInfo> executors() throws JSONException {
        return executors(null);
    }
    
    private JSONObject fetch(final String url) {
        Preconditions.checkState(!Strings.isNullOrEmpty(url));
        return Client.create().resource(url).get(JSONObject.class);
    }
    
    private Collection<JSONObject> findExecutors(final JSONArray frameworks, final String appName) throws JSONException {
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        String frameworkID;
        if (frameworkIDOptional.isPresent()) {
            frameworkID = frameworkIDOptional.get();
        } else {
            return Collections.emptyList();
        }
        List<JSONObject> result = new LinkedList<>();
        for (int i = 0; i < frameworks.length(); i++) {
            JSONObject framework = frameworks.getJSONObject(i);
            if (!framework.getString("id").equals(frameworkID)) {
                continue;
            }
            JSONArray executors = framework.getJSONArray("executors");
            for (int j = 0; j < executors.length(); j++) {
                JSONObject executor = executors.getJSONObject(j);
                if (null == appName || appName.equals(getExecutorId(executor).split("@-@")[0])) {
                    result.add(executor);
                }
            }
        }
        return result;
    }
    
    private String getExecutorId(final JSONObject executor) throws JSONException {
        return executor.has("id") ? executor.getString("id") : executor.getString("executor_id");
    }
    
    @Builder
    @Getter
    public static final class ExecutorStateInfo {
        
        private final String id;
        
        private final String slaveId;
    }
}
