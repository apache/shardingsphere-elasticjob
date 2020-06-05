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

package org.apache.shardingsphere.elasticjob.lite.internal.monitor;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.util.SensitiveInfoUtils;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitor service.
 */
@Slf4j
public final class MonitorService {
    
    public static final String DUMP_COMMAND = "dump";
    
    private final String jobName;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    private ServerSocket serverSocket;
    
    private volatile boolean closed;
    
    public MonitorService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        this.regCenter = regCenter;
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * start to listen.
     */
    public void listen() {
        int port = configService.load(true).getMonitorPort();
        if (port < 0) {
            return;
        }
        try {
            log.info("Elastic job: Monitor service is running, the port is '{}'", port);
            openSocketForMonitor(port);
        } catch (final IOException ex) {
            log.error("Elastic job: Monitor service listen failure, error is: ", ex);
        }
    }
    
    private void openSocketForMonitor(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(() -> {
            while (!closed) {
                try {
                    process(serverSocket.accept());
                } catch (final IOException ex) {
                    log.error("Elastic job: Monitor service open socket for monitor failure, error is: ", ex);
                }
            }
        }).start();
    }
    
    private void process(final Socket socket) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Socket ignored = socket) {
            String cmdLine = reader.readLine();
            if (null != cmdLine && DUMP_COMMAND.equalsIgnoreCase(cmdLine)) {
                List<String> result = new ArrayList<>();
                dumpDirectly("/" + jobName, result);
                outputMessage(writer, Joiner.on("\n").join(SensitiveInfoUtils.filterSensitiveIps(result)) + "\n");
            }
        }
    }
    
    private void dumpDirectly(final String path, final List<String> result) {
        for (String each : regCenter.getChildrenKeys(path)) {
            String zkPath = path + "/" + each;
            String zkValue = regCenter.get(zkPath);
            if (null == zkValue) {
                zkValue = "";
            }
            TreeCache treeCache = (TreeCache) regCenter.getRawCache("/" + jobName);
            ChildData treeCacheData = treeCache.getCurrentData(zkPath);
            String treeCachePath = null == treeCacheData ? "" : treeCacheData.getPath();
            String treeCacheValue = null == treeCacheData ? "" : new String(treeCacheData.getData());
            if (zkValue.equals(treeCacheValue) && zkPath.equals(treeCachePath)) {
                result.add(Joiner.on(" | ").join(zkPath, zkValue));
            } else {
                result.add(Joiner.on(" | ").join(zkPath, zkValue, treeCachePath, treeCacheValue));
            }
            dumpDirectly(zkPath, result);
        }
    }
    
    private void outputMessage(final BufferedWriter outputWriter, final String msg) throws IOException {
        outputWriter.append(msg);
        outputWriter.flush();
    }
    
    /**
     * Close listener.
     */
    public void close() {
        closed = true;
        if (null != serverSocket && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (final IOException ex) {
                log.error("Elastic job: Monitor service close failure, error is: ", ex);
            }
        }
    }
}
