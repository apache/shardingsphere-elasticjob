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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.error.handler.dingtalk.DingtalkPropertiesConstants;
import org.apache.shardingsphere.elasticjob.error.handler.email.EmailPropertiesConstants;
import org.apache.shardingsphere.elasticjob.error.handler.wechat.WechatPropertiesConstants;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.example.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.lite.example.job.simple.JavaOccurErrorJob;
import org.apache.shardingsphere.elasticjob.lite.example.job.simple.JavaSimpleJob;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public final class JavaMain {
    
    private static final int EMBED_ZOOKEEPER_PORT = 4181;
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:" + EMBED_ZOOKEEPER_PORT;
    
    private static final String JOB_NAMESPACE = "elasticjob-example-lite-java";
    
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
        TracingConfiguration<DataSource> tracingConfig = new TracingConfiguration<>("RDB", setUpEventTraceDataSource());
        setUpHttpJob(regCenter, tracingConfig);
        setUpSimpleJob(regCenter, tracingConfig);
        setUpDataflowJob(regCenter, tracingConfig);
        setUpScriptJob(regCenter, tracingConfig);
        setUpOneOffJob(regCenter, tracingConfig);
//        setUpOneOffJobWithEmail(regCenter, tracingConfig);
//        setUpOneOffJobWithDingtalk(regCenter, tracingConfig);
//        setUpOneOffJobWithWechat(regCenter, tracingConfig);
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
    
    private static void setUpHttpJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        new ScheduleJobBootstrap(regCenter, "HTTP", JobConfiguration.newBuilder("javaHttpJob", 3)
                .setProperty(HttpJobProperties.URI_KEY, "https://github.com")
                .setProperty(HttpJobProperties.METHOD_KEY, "GET")
                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").addExtraConfigurations(tracingConfig).build()).schedule();
        
    }
    
    private static void setUpSimpleJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        new ScheduleJobBootstrap(regCenter, new JavaSimpleJob(), JobConfiguration.newBuilder("javaSimpleJob", 3)
                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").addExtraConfigurations(tracingConfig).build()).schedule();
    }
    
    private static void setUpDataflowJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        new ScheduleJobBootstrap(regCenter, new JavaDataflowJob(), JobConfiguration.newBuilder("javaDataflowElasticJob", 3)
                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                .setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).addExtraConfigurations(tracingConfig).build()).schedule();
    }

    private static void setUpOneOffJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        new OneOffJobBootstrap(regCenter, new JavaSimpleJob(), JobConfiguration.newBuilder("javaOneOffSimpleJob", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").addExtraConfigurations(tracingConfig).build()).execute();
    }
    
    private static void setUpScriptJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) throws IOException {
        new ScheduleJobBootstrap(regCenter, "SCRIPT", JobConfiguration.newBuilder("scriptElasticJob", 3)
                .cron("0/5 * * * * ?").setProperty(ScriptJobProperties.SCRIPT_KEY, buildScriptCommandLine()).addExtraConfigurations(tracingConfig).build()).schedule();
    }
    
    private static void setUpOneOffJobWithEmail(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfEmailJob", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("EMAIL").addExtraConfigurations(tracingConfig).build();
        setEmailProperties(jobConfig);
        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
    }
    
    private static void setUpOneOffJobWithDingtalk(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfDingtalkJob", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("DINGTALK").addExtraConfigurations(tracingConfig).build();
        setDingtalkProperties(jobConfig);
        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
    }
    
    private static void setUpOneOffJobWithWechat(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfWechatJob", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("WECHAT").addExtraConfigurations(tracingConfig).build();
        setWechatProperties(jobConfig);
        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
    }
    
    private static void setEmailProperties(final JobConfiguration jobConfig) {
        jobConfig.getProps().setProperty(EmailPropertiesConstants.HOST, "host");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.PORT, "465");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.USERNAME, "username");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.PASSWORD, "password");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.FROM, "from@xxx.xx");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.TO, "to1@xxx.xx,to1@xxx.xx");
    }
    
    private static void setDingtalkProperties(final JobConfiguration jobConfig) {
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.WEBHOOK, "https://oapi.dingtalk.com/robot/send?access_token=token");
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.KEYWORD, "keyword");
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.SECRET, "secret");
    }
    
    private static void setWechatProperties(final JobConfiguration jobConfig) {
        jobConfig.getProps().setProperty(WechatPropertiesConstants.WEBHOOK, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=key");
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
