/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.ha.FrameworkIDService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Mesos状态服务.
 * 
 * @author gaohongtao
 */
@Slf4j
public class MesosStateService {
    
    private static String stateUrl;
    
    private final String frameworkID;
    
    public MesosStateService(final CoordinatorRegistryCenter regCenter) {
        Optional<String> frameworkIDOptional = new FrameworkIDService(regCenter).fetch();
        Preconditions.checkState(frameworkIDOptional.isPresent());
        this.frameworkID = frameworkIDOptional.get();
    }
    
    /**
     * 注册Mesos的Master信息.
     * 
     * @param hostName Master的主机名
     * @param port Master端口
     */
    public static synchronized void register(final String hostName, final int port) {
        stateUrl = String.format("http://%s:%d/state", hostName, port);
    }
    
    /**
     * 注销Mesos的Master信息.
     */
    public static synchronized void deregister() {
        stateUrl = null;
    }
    
    /**
     * 获取沙箱信息.
     * 
     * @param appName 作业云配置App的名字
     * @return 沙箱信息
     * @throws JSONException 解析JSON格式异常
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
     * 查找执行器信息.
     * 
     * @param appName 作业云配置App的名字
     * @return 执行器信息
     * @throws JSONException 解析JSON格式异常
     */
    public Collection<ExecutorInfo> executors(final String appName) throws JSONException {
        return Collections2.transform(findExecutors(fetch(stateUrl).getJSONArray("frameworks"), appName), new Function<JSONObject, ExecutorInfo>() {
            @Override
            public ExecutorInfo apply(final JSONObject input) {
                try {
                    return ExecutorInfo.builder().id(getExecutorId(input)).slaveId(input.getString("slave_id")).build();
                } catch (final JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    
    private JSONObject fetch(final String url) {
        Preconditions.checkState(!Strings.isNullOrEmpty(url));
        return Client.create().resource(url).get(JSONObject.class);
    }
    
    private Collection<JSONObject> findExecutors(final JSONArray frameworks, final String appName) throws JSONException {
        List<JSONObject> result = Lists.newArrayList();
        for (int i = 0; i < frameworks.length(); i++) {
            JSONObject framework = frameworks.getJSONObject(i);
            if (!framework.getString("id").equals(frameworkID)) {
                continue;
            }
            JSONArray executors = framework.getJSONArray("executors");
            for (int j = 0; j < executors.length(); j++) {
                JSONObject executor = executors.getJSONObject(j);
                if (appName.equals(getExecutorId(executor).split("@-@")[0])) {
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
    public static final class ExecutorInfo {
        
        private final String id;
        
        private final String slaveId;
    }
}
