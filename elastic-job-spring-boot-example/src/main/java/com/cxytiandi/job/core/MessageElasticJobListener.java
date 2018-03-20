package com.cxytiandi.job.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cxytiandi.job.util.DingDingMessageUtil;
import com.cxytiandi.job.util.JsonUtils;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

/**
 * 作业监听器, 执行前后发送钉钉消息进行通知
 * @author yinjihuan
 */
public class MessageElasticJobListener implements ElasticJobListener {

    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String msg = date + " 【猿天地-" + shardingContexts.getJobName() + "】任务开始执行====" + JsonUtils.toJson(shardingContexts);
        DingDingMessageUtil.sendTextMessage(msg);
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
    	String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String msg = date + " 【猿天地-" + shardingContexts.getJobName() + "】任务执行结束====" + JsonUtils.toJson(shardingContexts);
        DingDingMessageUtil.sendTextMessage(msg);
    }

}