package com.dangdang.ddframe.job.api;

/**
 * simple 类型的任务接口.
 * 
 * @author zhong
 */
public interface SimpleElasticJob extends ElasticJob {

	/**
	 * 所有分片都执行完毕后，处理收尾任务.
	 */
    public void afterAllShardingFinished();
}
