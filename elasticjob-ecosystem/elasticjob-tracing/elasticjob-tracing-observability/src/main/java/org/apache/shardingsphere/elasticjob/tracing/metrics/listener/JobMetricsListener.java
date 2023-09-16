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

package org.apache.shardingsphere.elasticjob.tracing.metrics.listener;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.listener.TracingListener;
import org.apache.shardingsphere.elasticjob.tracing.metrics.binder.JobMetrics;
import org.apache.shardingsphere.elasticjob.tracing.metrics.config.MetricConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class JobMetricsListener implements TracingListener {
    private static final Logger logger = LoggerFactory.getLogger(JobMetricsListener.class);

    private JobMetrics jobMetrics;

    CompositeMeterRegistry composite = new CompositeMeterRegistry();


    public JobMetricsListener(MetricConfig metricConfig) {
        //open jvm http port to export metrics
        jobMetrics = new JobMetrics();


        //开启一个服务，用于暴露指标
        HttpServer server = null;
        try {
            PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            composite.add(prometheusRegistry);

            server = HttpServer.create(new InetSocketAddress(8081), 0);

            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            //开启服务
            new Thread(server::start).start();
        } catch (IOException e) {
            logger.error("metrics start error:{}", e.getMessage(), e);
        }
        HttpServer finalServer = server;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalServer != null) {
                finalServer.stop(3000);
            }
        }));

    }

    @Override
    public void listen(JobExecutionEvent jobExecutionEvent) {

    }

    @Override
    public void listen(JobStatusTraceEvent jobStatusTraceEvent) {
        if (jobStatusTraceEvent == null || StringUtils.isBlank(jobStatusTraceEvent.getJobName())) {
            return;
        }
        jobMetrics.increase(jobStatusTraceEvent.getJobName(), jobStatusTraceEvent.getState());
    }
}
