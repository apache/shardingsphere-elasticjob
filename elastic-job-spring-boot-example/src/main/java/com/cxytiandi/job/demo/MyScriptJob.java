package com.cxytiandi.job.demo;

import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.script.ScriptJob;
/**
 * 脚本任务不需要写逻辑，逻辑在被执行的脚本中，这边只是定义一个任务而已
 * @author yinjihuan
 *
 */
@ElasticJobConf(name = "MyScriptJob")
public class MyScriptJob implements ScriptJob {

	public void execute(ShardingContext context) {
		
	}

}
