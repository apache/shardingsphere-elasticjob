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

package com.dangdang.ddframe.job.cloud.agent;

import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.cloud.agent.executor.DaemonTaskExecutor;
import com.dangdang.ddframe.job.cloud.agent.executor.TransientTaskExecutor;
import com.dangdang.ddframe.job.cloud.agent.internal.ArgumentsParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.Executor;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

/**
 * 云作业启动器.
 * 
 * <p>需将应用打包, 并在main方法中直接调用Bootstrap.execute方法</p>
 *
 * @author caohao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    /**
     * 执行作业.
     * 
     * @param args 命令行参数
     * @throws JobExecutionEnvironmentException 作业执行环境异常
     */
    public static void execute(final String[] args) throws JobExecutionEnvironmentException {
        ArgumentsParser parser = ArgumentsParser.parse(args);
        Executor executor;
        if (parser.getJobConfig().isTransient()) {
            executor = new TransientTaskExecutor(parser.getElasticJob(), parser.getShardingContext(), parser.getJobConfig());
        } else {
            executor = new DaemonTaskExecutor(parser.getElasticJob(), parser.getShardingContext(), parser.getJobConfig());
        }
        MesosExecutorDriver driver = new MesosExecutorDriver(executor);
        System.exit(Protos.Status.DRIVER_STOPPED == driver.run() ? 0 : -1);
    }
}
