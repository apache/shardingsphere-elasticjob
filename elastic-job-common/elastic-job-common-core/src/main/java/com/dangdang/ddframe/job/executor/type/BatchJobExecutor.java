package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.batch.BatchJob;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.executor.JobFacade;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class BatchJobExecutor extends AbstractElasticJobExecutor {

    private final BatchJob<Object> batchJob;

    public BatchJobExecutor(final BatchJob<Object> batchJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.batchJob = batchJob;
    }


    @Override
    protected void process(ShardingContext shardingContext) {
        Set<Integer> shardingItems = shardingContext.getShardingItemParameters().keySet();
        int shardingItem = shardingContext.getShardingItem();

        // 找出当前实例的最大分片
        int maxItem = Collections.max(shardingItems);

        if (shardingItem != maxItem) {
            log.info("分片 {} 跳过执行，本批次由最大分片 {} 代理执行。", shardingItem, maxItem);
            return;
        }

        log.info("分片 {} 开始执行批次任务，负责处理 {} 个分片。", shardingItem, shardingItems.size());
        try {
            List<Object> data = batchJob.fetchData(shardingItems);
            batchJob.processData(data);
        } catch (Exception e) {
            log.error("分片 {} 执行批次任务失败", shardingItem, e);
            throw e;
        }
    }
}
