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

package org.apache.shardingsphere.elasticjob.test.natived.commons.http;

import com.sun.net.httpserver.HttpServer;
import org.awaitility.Awaitility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local HTTP server which records the paths of the requests sent by HTTP jobs in native tests.
 *
 * <p>Each job uses its own path, named after the job, so that requests of other tests are never mistaken for its requests.</p>
 */
public final class LocalHttpServer {
    
    private static final Set<String> REQUESTED_PATHS = ConcurrentHashMap.newKeySet();
    
    private static HttpServer httpServer;
    
    private LocalHttpServer() {
    }
    
    /**
     * Start the server on a free port of the loopback address. Starting a started server does nothing.
     *
     * @throws IOException I/O exception
     */
    public static synchronized void start() throws IOException {
        if (null != httpServer) {
            return;
        }
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/", exchange -> {
            REQUESTED_PATHS.add(exchange.getRequestURI().getPath());
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        httpServer.start();
    }
    
    /**
     * Stop the server.
     */
    public static synchronized void stop() {
        if (null != httpServer) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
    
    /**
     * Get the URI of a job, which is a path of the server named after the job.
     *
     * @param jobName job name
     * @return URI of the job
     */
    public static synchronized String jobUri(final String jobName) {
        return "http://127.0.0.1:" + httpServer.getAddress().getPort() + "/" + jobName;
    }
    
    /**
     * Wait until the server receives a request of the job.
     *
     * @param jobName job name
     */
    public static void awaitRequest(final String jobName) {
        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> REQUESTED_PATHS.contains("/" + jobName));
    }
}
