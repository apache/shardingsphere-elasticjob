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

package io.elasticjob.lite.internal.guarantee;

import com.google.common.collect.Lists;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 保证分布式任务全部开始和结束状态的服务.
 * 
 * @author zhangliang
 */
public final class GuaranteeService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    public GuaranteeService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * 根据分片项注册任务开始运行.
     * 
     * @param shardingItems 待注册的分片项
     */
    public void registerStart(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getStartedNode(each));
        }
    }
    
    /**
     * 判断是否所有的任务均启动完毕.
     *
     * @return 是否所有的任务均启动完毕
     */
    public boolean isAllStarted() {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.STARTED_ROOT)
                && configService.load(false).getTypeConfig().getCoreConfig().getShardingTotalCount()
                == jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.STARTED_ROOT).size();
    }
    
    /**
     * 判断是否有资格执行分布式任务.
     *
     * @return 是否获取资格执行分布式任务
     */
    public boolean isQualifiedBeforeAllStarted() {
        String sequentialJobNode = jobNodeStorage.createEphemeralSequentialJobNode(GuaranteeNode.STARTED_SEQUENTIAL_NODE);
        if (sequentialJobNode == null || sequentialJobNode.isEmpty()) {
            return false;
        }
        List<String> children = jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.STARTED_SEQUENTIAL_ROOT);
        if (children != null && children.size() > 0 && jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.STARTED_ROOT).size() > 0) {
            List<String> sortedList = Lists.newArrayList(children);
            Collections.sort(sortedList,
                new Comparator<String>() {
                    @Override
                    public int compare(final String lhs, final String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });
            return sortedList.get(0) != null && sequentialJobNode.endsWith(sortedList.get(0));
        }
        return false;
    }
    
    /**
     * 清理所有任务启动信息.
     */
    public void clearAllStartedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.STARTED_ROOT);
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.STARTED_SEQUENTIAL_ROOT);
    }
    
    /**
     * 根据分片项注册任务完成运行.
     *
     * @param shardingItems 待注册的分片项
     */
    public void registerComplete(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getCompletedNode(each));
        }
    }
    
    /**
     * 判断是否所有的任务均执行完毕.
     *
     * @return 是否所有的任务均执行完毕
     */
    public boolean isAllCompleted() {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.COMPLETED_ROOT)
                && configService.load(false).getTypeConfig().getCoreConfig().getShardingTotalCount()
                <= jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.COMPLETED_ROOT).size();
    }
    
    /**
     * 判断是否有资格执行分布式任务.
     *
     * @return 是否获取资格执行分布式任务
     */
    public boolean isQualifiedAfterAllCompleted() {
        String sequentialJobNode = jobNodeStorage.createEphemeralSequentialJobNode(GuaranteeNode.COMPLETED_SEQUENTIAL_NODE);
        if (sequentialJobNode == null || sequentialJobNode.isEmpty()) {
            return false;
        }
        List<String> children = jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.COMPLETED_SEQUENTIAL_ROOT);
        if (children != null && children.size() > 0 && jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.COMPLETED_ROOT).size() > 0) {
            List<String> sortedList = Lists.newArrayList(children);
            Collections.sort(sortedList,
                    new Comparator<String>() {
                        @Override
                        public int compare(final String lhs, final String rhs) {
                            return lhs.compareTo(rhs);
                        }
                    });
            return sortedList.get(0) != null && sequentialJobNode.endsWith(sortedList.get(0));
        }
        return false;
    }
    
    /**
     * 清理所有任务启动信息.
     */
    public void clearAllCompletedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.COMPLETED_ROOT);
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.COMPLETED_SEQUENTIAL_ROOT);
    }
}
