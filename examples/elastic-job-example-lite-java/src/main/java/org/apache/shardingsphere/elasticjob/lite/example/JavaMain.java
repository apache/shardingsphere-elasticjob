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

package org.apache.shardingsphere.elasticjob.lite.example;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.lite.api.JobScheduler;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.example.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.lite.example.job.simple.JavaSimpleJob;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public final class JavaMain {
    
    private static final int EMBED_ZOOKEEPER_PORT = 4181;
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:" + EMBED_ZOOKEEPER_PORT;
    
    private static final String JOB_NAMESPACE = "elastic-job-example-lite-java";
    
    // switch to MySQL by yourself
//    private static final String EVENT_RDB_STORAGE_DRIVER = "com.mysql.jdbc.Driver";
//    private static final String EVENT_RDB_STORAGE_URL = "jdbc:mysql://localhost:3306/elastic_job_log";
    
    private static final String EVENT_RDB_STORAGE_DRIVER = "org.h2.Driver";
    
    private static final String EVENT_RDB_STORAGE_URL = "jdbc:h2:mem:job_event_storage";
    
    private static final String EVENT_RDB_STORAGE_USERNAME = "sa";
    
    private static final String EVENT_RDB_STORAGE_PASSWORD = "";
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
    // CHECKSTYLE:ON
        EmbedZookeeperServer.start(EMBED_ZOOKEEPER_PORT);
        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();
        TracingConfiguration tracingConfig = new TracingConfiguration<>("RDB", setUpEventTraceDataSource());
        setUpSimpleJob(regCenter, tracingConfig);
        setUpDataflowJob(regCenter, tracingConfig);
        setUpScriptJob(regCenter, tracingConfig);
    }
    
    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, JOB_NAMESPACE);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }
    
    private static DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(EVENT_RDB_STORAGE_DRIVER);
        result.setUrl(EVENT_RDB_STORAGE_URL);
        result.setUsername(EVENT_RDB_STORAGE_USERNAME);
        result.setPassword(EVENT_RDB_STORAGE_PASSWORD);
        return result;
    }
    
    private static void setUpSimpleJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration tracingConfig) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder("javaSimpleJob", JobType.SIMPLE, "0/5 * * * * ?", 3).shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").build();
        new JobScheduler(regCenter, new JavaSimpleJob(), JobConfiguration.newBuilder(coreConfig).build(), tracingConfig).init();
    }
    
    private static void setUpDataflowJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration tracingConfig) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder(
                "javaDataflowElasticJob", JobType.DATAFLOW, "0/5 * * * * ?", 3).shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                .setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).build();
        new JobScheduler(regCenter, new JavaDataflowJob(), JobConfiguration.newBuilder(coreConfig).build(), tracingConfig).init();
    }
    
    private static void setUpScriptJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration tracingConfig) throws IOException {
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder(
                "scriptElasticJob", JobType.SCRIPT, "0/5 * * * * ?", 3).setProperty(ScriptJobExecutor.SCRIPT_KEY, buildScriptCommandLine()).build();
        new JobScheduler(regCenter, null, JobConfiguration.newBuilder(coreConfig).build(), tracingConfig).init();
    }
    
    private static String buildScriptCommandLine() throws IOException {
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return Paths.get(JavaMain.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
        }
        Path result = Paths.get(JavaMain.class.getResource("/script/demo.sh").getPath());
        Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
        return result.toString();
    }
}
