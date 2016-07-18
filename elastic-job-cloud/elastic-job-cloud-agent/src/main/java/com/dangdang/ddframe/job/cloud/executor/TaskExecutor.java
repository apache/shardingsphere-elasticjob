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
 */

package com.dangdang.ddframe.job.cloud.executor;

import lombok.RequiredArgsConstructor;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;

import com.dangdang.ddframe.job.context.ShardingContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 作业启动执行器.
 *
 * @author caohao
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TaskExecutor implements Executor {
    
    private final ShardingContext shardingContext;
    
    @Override
    public void registered(final ExecutorDriver executorDriver, final ExecutorInfo executorInfo, final FrameworkInfo frameworkInfo, final SlaveInfo slaveInfo) {
    }
    
    @Override
    public void reregistered(final ExecutorDriver executorDriver, final SlaveInfo slaveInfo) {
    }
    
    @Override
    public void disconnected(final ExecutorDriver executorDriver) {
    }
    
    // TODO 解析作业和执行作业是否可分开, 解析作业放入registered. 需调研mesos executor生命周期
    @Override
    public void launchTask(final ExecutorDriver executorDriver, final TaskInfo taskInfo) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("conf/job.properties"));
            String[] jobClasses = properties.getProperty("job.classes").split(",");
            for (String each : jobClasses) {
                Class<?> cloudElasticJobClass = Class.forName(each);
                Object cloudElasticJob = cloudElasticJobClass.getConstructor(ShardingContext.class).newInstance(shardingContext);
                cloudElasticJobClass.getMethod("execute").invoke(cloudElasticJob);
            }
            executorDriver.sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_FINISHED).build());
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
