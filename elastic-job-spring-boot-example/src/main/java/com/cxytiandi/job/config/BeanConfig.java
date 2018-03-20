package com.cxytiandi.job.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

@Configuration
public class BeanConfig {
	
	/**
	 * 任务执行事件数据源
	 * @return
	 */
	@Bean("datasource")
	@ConfigurationProperties("spring.datasource.druid.log")
	public DataSource dataSourceTwo(){
	    return DruidDataSourceBuilder.create().build();
	}
	
}
