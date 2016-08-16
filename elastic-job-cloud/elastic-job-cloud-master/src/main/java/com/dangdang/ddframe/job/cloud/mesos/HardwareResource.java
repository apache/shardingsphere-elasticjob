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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.config.impl.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.api.config.impl.ShardingItemParameters;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.log.JobLogEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobRdbEventConfiguration;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.apache.mesos.Protos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 硬件资源.
 *
 * @author zhangliang
 */
@EqualsAndHashCode(of = "offerId")
public final class HardwareResource {
    
    private static final String RUN_COMMAND = "sh %s '%s'";
    
    private final Protos.Offer offer;
    
    private final String offerId;
    
    private BigDecimal availableCpuCount;
    
    private BigDecimal availableMemoryMB;
    
    private BigDecimal reservedCpuCount;
    
    private BigDecimal reservedMemoryMB;
    
    public HardwareResource(final Protos.Offer offer) {
        this.offer = offer;
        offerId = offer.getId().getValue();
        availableCpuCount = getResource("cpus");
        availableMemoryMB = getResource("mem");
        reservedCpuCount = BigDecimal.ZERO;
        reservedMemoryMB = BigDecimal.ZERO;
    }
    
    private BigDecimal getResource(final String type) {
        for (Protos.Resource each : offer.getResourcesList()) {
            if (type.equals(each.getName())) {
                return new BigDecimal(Double.toString(each.getScalar().getValue()));
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 根据需资源计算可以分多少片.
     *
     * @param expectedShardingCount 期望的分片总数
     * @param perCpuCount 每片需要使用的CPU数量
     * @param perMemoryMB 每片需要使用的内存兆字节数量
     * @return 分片数量
     */
    public int calculateShardingCount(final int expectedShardingCount, final double perCpuCount, final double perMemoryMB) {
        int cpuShardingCount = availableCpuCount.divide(new BigDecimal(Double.toString(perCpuCount)), BigDecimal.ROUND_DOWN).intValue();
        if (cpuShardingCount > expectedShardingCount) {
            cpuShardingCount = expectedShardingCount;
        }
        int memoryShardingCount = availableMemoryMB.divide(new BigDecimal(Double.toString(perMemoryMB)), BigDecimal.ROUND_DOWN).intValue();
        if (memoryShardingCount > expectedShardingCount) {
            memoryShardingCount = expectedShardingCount;
        }
        return cpuShardingCount < memoryShardingCount ? cpuShardingCount : memoryShardingCount;
    }
    
    /**
     * 预留资源.
     *
     * @param toBeReservedCpuCount 需预留的CPU数量
     * @param toBeReservedMemoryMB 需预留的内存兆字节数量
     */
    public void reserveResources(final double toBeReservedCpuCount, final double toBeReservedMemoryMB) {
        BigDecimal toBeReservedCpuCountDecimal = new BigDecimal(Double.toString(toBeReservedCpuCount));
        BigDecimal toBeReservedMemoryMBDecimal = new BigDecimal(Double.toString(toBeReservedMemoryMB));
        Preconditions.checkArgument(availableCpuCount.doubleValue() >= toBeReservedCpuCountDecimal.add(reservedCpuCount).doubleValue());
        Preconditions.checkArgument(availableMemoryMB.doubleValue() >= toBeReservedMemoryMBDecimal.add(reservedMemoryMB).doubleValue());
        reservedCpuCount = reservedCpuCount.add(toBeReservedCpuCountDecimal);
        reservedMemoryMB = reservedMemoryMB.add(toBeReservedMemoryMBDecimal);
    }
    
    /**
     * 提交预留资源.
     */
    public void commitReservedResources() {
        availableCpuCount = availableCpuCount.subtract(reservedCpuCount);
        availableMemoryMB = availableMemoryMB.subtract(reservedMemoryMB);
        reservedCpuCount = BigDecimal.ZERO;
        reservedMemoryMB = BigDecimal.ZERO;
    }
    
    /**
     * 创建Mesos任务对象.
     *
     * @param jobContext 云作业配置
     * @param shardingItem 分片项
     * @return 任务对象
     */
    public Protos.TaskInfo createTaskInfo(final JobContext jobContext, final int shardingItem) {
        CloudJobConfiguration jobConfig = jobContext.getJobConfig();
        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(new TaskContext(jobConfig.getJobName(), shardingItem, jobContext.getType(), offer.getSlaveId().getValue()).getId()).build();
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(jobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        Map<Integer, String> assignedShardingItemParameters = new HashMap<>(1, 1);
        assignedShardingItemParameters.put(shardingItem, shardingItemParameters.containsKey(shardingItem) ? shardingItemParameters.get(shardingItem) : "");
        ShardingContext shardingContext = new ShardingContext(
                jobConfig.getJobName(), jobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), jobConfig.getTypeConfig().getCoreConfig().getJobParameter(), assignedShardingItemParameters);
        // TODO 更改cache为elastic-job-cloud.properties配置
        Protos.CommandInfo.URI uri = Protos.CommandInfo.URI.newBuilder().setValue(jobConfig.getAppURL()).setExtract(true).setCache(false).build();
        Protos.CommandInfo command = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true).setValue(
                String.format(RUN_COMMAND, jobConfig.getBootstrapScript(), toCloudJobParameterJson(shardingContext, jobConfig))).build();
        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder().setValue(taskId.getValue())).setCommand(command).build();
        return Protos.TaskInfo.newBuilder()
                .setName(taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", jobConfig.getCpuCount()))
                .addResources(buildResource("mem", jobConfig.getMemoryMB()))
                .setExecutor(executorInfo)
                .build();
    }
    
    private String toCloudJobParameterJson(final ShardingContext shardingContext, final CloudJobConfiguration jobConfig) {
        Map<String, Object> cloudJobParameterMap = new LinkedHashMap<>(2, 1);
        cloudJobParameterMap.put("shardingContext", shardingContext);
        cloudJobParameterMap.put("jobConfigContext", buildJobConfigurationContext(jobConfig));
        return GsonFactory.getGson().toJson(cloudJobParameterMap);
    }
    
    private Map<String, String> buildJobConfigurationContext(final CloudJobConfiguration jobConfig) {
        Map<String, String> result = new LinkedHashMap<>(16, 1);
        result.put("jobType", jobConfig.getTypeConfig().getJobType().name());
        result.put("jobName", jobConfig.getJobName());
        result.put("jobClass", jobConfig.getTypeConfig().getJobClass());
        result.put("cron", JobExecutionType.DAEMON == jobConfig.getJobExecutionType() ? jobConfig.getTypeConfig().getCoreConfig().getCron().replaceAll("\\s+", "&nbsp;") : "");
        result.put("jobExceptionHandler", jobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER));
        result.put("executorServiceHandler", jobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER));
        if (jobConfig.getTypeConfig() instanceof DataflowJobConfiguration) {
            result.put("dataflowType", ((DataflowJobConfiguration) jobConfig.getTypeConfig()).getDataflowType().name());
            result.put("streamingProcess", ((DataflowJobConfiguration) jobConfig.getTypeConfig()).isStreamingProcess() + "");
        } else if (jobConfig.getTypeConfig() instanceof ScriptJobConfiguration) {
            result.put("scriptCommandLine", ((ScriptJobConfiguration) jobConfig.getTypeConfig()).getScriptCommandLine());
        }
        result.putAll(buildJobEventConfiguration(jobConfig));
        return result;
    }
    
    private Map<String, String> buildJobEventConfiguration(final CloudJobConfiguration jobConfig) {
        Map<String, String> result = new LinkedHashMap<>(6, 1);
        Map<String, JobEventConfiguration> configurations = jobConfig.getTypeConfig().getCoreConfig().getJobEventConfigs();
        for (JobEventConfiguration each : configurations.values()) {
            if (each instanceof JobRdbEventConfiguration) {
                JobRdbEventConfiguration rdbEventConfig = (JobRdbEventConfiguration) each;
                result.put("driverClassName", rdbEventConfig.getDriverClassName());
                result.put("url", rdbEventConfig.getUrl());
                result.put("username", rdbEventConfig.getUsername());
                result.put("password", rdbEventConfig.getPassword());
                result.put("logLevel", rdbEventConfig.getLogLevel().name());
            }
            if (each instanceof JobLogEventConfiguration) {
                result.put("logEvent", "true");
            }
        }
        return result;
    }
    
    private Protos.Resource.Builder buildResource(final String type, final double resourceValue) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue));
    }
}
