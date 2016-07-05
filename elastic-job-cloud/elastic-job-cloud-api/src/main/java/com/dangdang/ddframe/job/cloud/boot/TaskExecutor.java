/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.cloud.boot;

import lombok.RequiredArgsConstructor;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 云作业启动执行器.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class TaskExecutor implements Executor {
    
    private final String taskId;
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        MesosExecutorDriver driver = new MesosExecutorDriver(new TaskExecutor(args[0]));
        System.exit(driver.run() == Status.DRIVER_STOPPED ? 0 : 1);
    }
    
    @Override
    public void registered(final ExecutorDriver executorDriver, final ExecutorInfo executorInfo, final FrameworkInfo frameworkInfo, final SlaveInfo slaveInfo) {
        
    }
    
    @Override
    public void reregistered(final ExecutorDriver executorDriver, final SlaveInfo slaveInfo) {
        
    }
    
    @Override
    public void disconnected(final ExecutorDriver executorDriver) {
        
    }
    
    @Override
    public void launchTask(final ExecutorDriver executorDriver, final TaskInfo taskInfo) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("conf/job.properties"));
            String[] jobClassNames = properties.getProperty("jobClassNames").split(",");
            for (String each : jobClassNames) {
                Class<?> cloudElasticJobClass = Class.forName(each);
                Object cloudElasticJob = cloudElasticJobClass.getConstructor(String.class).newInstance(taskId);
                cloudElasticJobClass.getMethod("execute").invoke(cloudElasticJob);
            }
        } catch (final IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void killTask(final ExecutorDriver executorDriver, final TaskID taskID) {
        
    }
    
    @Override
    public void frameworkMessage(final ExecutorDriver executorDriver, final byte[] bytes) {
        
    }
    
    @Override
    public void shutdown(final ExecutorDriver executorDriver) {
        
    }
    
    @Override
    public void error(final ExecutorDriver executorDriver, final String s) {
        
    }
}
