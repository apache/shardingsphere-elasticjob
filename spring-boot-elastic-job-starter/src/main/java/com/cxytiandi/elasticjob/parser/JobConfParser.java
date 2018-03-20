package com.cxytiandi.elasticjob.parser;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.cxytiandi.elasticjob.base.JobAttributeTag;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
/**
 * Job解析类
 * 
 * <p>从注解中解析任务信息初始化<p>
 * 
 * @author yinjihuan
 *
 * @about http://cxytiandi.com/about
 */
@Component
@Configuration
public class JobConfParser implements ApplicationContextAware {
	
	private Logger logger = LoggerFactory.getLogger(JobConfParser.class);
	
	@Autowired
	private ZookeeperRegistryCenter zookeeperRegistryCenter;

	private String prefix = "elasticJob.";
	
	private Environment environment;
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		environment = ctx.getEnvironment();
		Map<String, Object> beanMap = ctx.getBeansWithAnnotation(ElasticJobConf.class);
		for (Object confBean : beanMap.values()) {
			Class<?> clz = confBean.getClass();
			String jobTypeName = confBean.getClass().getInterfaces()[0].getSimpleName();
			ElasticJobConf conf = clz.getAnnotation(ElasticJobConf.class);
			
			String jobClass = clz.getName();
			String jobName = conf.name();
			String cron = getEnvironmentStringValue(jobName, JobAttributeTag.CRON, conf.cron());
			String shardingItemParameters = getEnvironmentStringValue(jobName, JobAttributeTag.SHARDING_ITEM_PARAMETERS, conf.shardingItemParameters());
			String description = getEnvironmentStringValue(jobName, JobAttributeTag.DESCRIPTION, conf.description());
			String jobParameter = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_PARAMETER, conf.jobParameter());
			String jobExceptionHandler = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_EXCEPTION_HANDLER, conf.jobExceptionHandler());
			String executorServiceHandler = getEnvironmentStringValue(jobName, JobAttributeTag.EXECUTOR_SERVICE_HANDLER, conf.executorServiceHandler());
			
			String jobShardingStrategyClass = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_SHARDING_STRATEGY_CLASS, conf.jobShardingStrategyClass());
			String eventTraceRdbDataSource = getEnvironmentStringValue(jobName, JobAttributeTag.EVENT_TRACE_RDB_DATA_SOURCE, conf.eventTraceRdbDataSource());
			String scriptCommandLine = getEnvironmentStringValue(jobName, JobAttributeTag.SCRIPT_COMMAND_LINE, conf.scriptCommandLine());
			
			boolean failover = getEnvironmentBooleanValue(jobName, JobAttributeTag.FAILOVER, conf.failover());
			boolean misfire = getEnvironmentBooleanValue(jobName, JobAttributeTag.MISFIRE, conf.misfire());
			boolean overwrite = getEnvironmentBooleanValue(jobName, JobAttributeTag.OVERWRITE, conf.overwrite());
			boolean disabled = getEnvironmentBooleanValue(jobName, JobAttributeTag.DISABLED, conf.disabled());
			boolean monitorExecution = getEnvironmentBooleanValue(jobName, JobAttributeTag.MONITOR_EXECUTION, conf.monitorExecution());
			boolean streamingProcess = getEnvironmentBooleanValue(jobName, JobAttributeTag.STREAMING_PROCESS, conf.streamingProcess());
			
			int shardingTotalCount = getEnvironmentIntValue(jobName, JobAttributeTag.SHARDING_TOTAL_COUNT, conf.shardingTotalCount());
			int monitorPort = getEnvironmentIntValue(jobName, JobAttributeTag.MONITOR_PORT, conf.monitorPort());
			int maxTimeDiffSeconds = getEnvironmentIntValue(jobName, JobAttributeTag.MAX_TIME_DIFF_SECONDS, conf.maxTimeDiffSeconds());
			int reconcileIntervalMinutes = getEnvironmentIntValue(jobName, JobAttributeTag.RECONCILE_INTERVAL_MINUTES, conf.reconcileIntervalMinutes());
			
			// 核心配置
			JobCoreConfiguration coreConfig = 
					JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
					.shardingItemParameters(shardingItemParameters)
					.description(description)
					.failover(failover)
					.jobParameter(jobParameter)
					.misfire(misfire)
					.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandler)
					.jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandler)
					.build();
			
			// 不同类型的任务配置处理
			LiteJobConfiguration jobConfig = null;
			JobTypeConfiguration typeConfig = null;
			if (jobTypeName.equals("SimpleJob")) {
				typeConfig = new SimpleJobConfiguration(coreConfig, jobClass);
			}
			
			if (jobTypeName.equals("DataflowJob")) {
				typeConfig = new DataflowJobConfiguration(coreConfig, jobClass, streamingProcess);
			}

			if (jobTypeName.equals("ScriptJob")) {
				typeConfig = new ScriptJobConfiguration(coreConfig, scriptCommandLine);
			}
			
			jobConfig = LiteJobConfiguration.newBuilder(typeConfig)
					.overwrite(overwrite)
					.disabled(disabled)
					.monitorPort(monitorPort)
					.monitorExecution(monitorExecution)
					.maxTimeDiffSeconds(maxTimeDiffSeconds)
					.jobShardingStrategyClass(jobShardingStrategyClass)
					.reconcileIntervalMinutes(reconcileIntervalMinutes)
					.build();
		
			List<BeanDefinition> elasticJobListeners = getTargetElasticJobListeners(conf);
			
			// 构建SpringJobScheduler对象来初始化任务
			BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
	        factory.setInitMethodName("init");
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            if ("ScriptJob".equals(jobTypeName)) {
            	factory.addConstructorArgValue(null);
            } else {
            	factory.addConstructorArgValue(confBean);
            }
            factory.addConstructorArgValue(zookeeperRegistryCenter);
            factory.addConstructorArgValue(jobConfig);
            
            // 任务执行日志数据源，以名称获取
            if (StringUtils.hasText(eventTraceRdbDataSource)) {
            	BeanDefinitionBuilder rdbFactory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
            	rdbFactory.addConstructorArgReference(eventTraceRdbDataSource);
            	factory.addConstructorArgValue(rdbFactory.getBeanDefinition());
			}
            
            factory.addConstructorArgValue(elasticJobListeners);
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)ctx.getAutowireCapableBeanFactory();
			defaultListableBeanFactory.registerBeanDefinition("SpringJobScheduler", factory.getBeanDefinition());
			SpringJobScheduler springJobScheduler = (SpringJobScheduler) ctx.getBean("SpringJobScheduler");
			springJobScheduler.init();
			logger.info("【" + jobName + "】\t" + jobClass + "\tinit success");
		}
	}

	private List<BeanDefinition> getTargetElasticJobListeners(ElasticJobConf conf) {
        List<BeanDefinition> result = new ManagedList<BeanDefinition>(2);
        String listeners = getEnvironmentStringValue(conf.name(), JobAttributeTag.LISTENER, conf.listener());
        if (StringUtils.hasText(listeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listeners);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            result.add(factory.getBeanDefinition());
        }
        
        String distributedListeners = getEnvironmentStringValue(conf.name(), JobAttributeTag.DISTRIBUTED_LISTENER, conf.distributedListener());
        long startedTimeoutMilliseconds = getEnvironmentLongValue(conf.name(), JobAttributeTag.DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS, conf.startedTimeoutMilliseconds());
        long completedTimeoutMilliseconds = getEnvironmentLongValue(conf.name(), JobAttributeTag.DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS, conf.completedTimeoutMilliseconds());
        
        if (StringUtils.hasText(distributedListeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListeners);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            factory.addConstructorArgValue(startedTimeoutMilliseconds);
            factory.addConstructorArgValue(completedTimeoutMilliseconds);
            result.add(factory.getBeanDefinition());
        }
        return result;
	}
	 
	/**
	 * 获取配置中的任务属性值，environment没有就用注解中的值
	 * @param jobName		任务名称
	 * @param fieldName		属性名称
	 * @param defaultValue  默认值
	 * @return
	 */
	private String getEnvironmentStringValue(String jobName, String fieldName, String defaultValue) {
		String key = prefix + jobName + "." + fieldName;
		String value = environment.getProperty(key);
		if (StringUtils.hasText(value)) {
			return value;
		}
		return defaultValue;
	}
	
	private int getEnvironmentIntValue(String jobName, String fieldName, int defaultValue) {
		String key = prefix + jobName + "." + fieldName;
		String value = environment.getProperty(key);
		if (StringUtils.hasText(value)) {
			return Integer.valueOf(value);
		}
		return defaultValue;
	}
	
	private long getEnvironmentLongValue(String jobName, String fieldName, long defaultValue) {
		String key = prefix + jobName + "." + fieldName;
		String value = environment.getProperty(key);
		if (StringUtils.hasText(value)) {
			return Long.valueOf(value);
		}
		return defaultValue;
	}
	
	private boolean getEnvironmentBooleanValue(String jobName, String fieldName, boolean defaultValue) {
		String key = prefix + jobName + "." + fieldName;
		String value = environment.getProperty(key);
		if (StringUtils.hasText(value)) {
			return Boolean.valueOf(value);
		}
		return defaultValue;
	}
}
