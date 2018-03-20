package com.cxytiandi.job;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import com.cxytiandi.elasticjob.annotation.EnableElasticJob;

/**
 * ElasticJob Spring Boot集成案例
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 */
@SpringBootApplication
@EnableElasticJob
public class JobApplication {
	
	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(JobApplication.class).web(false).run(args);
		try {
			new CountDownLatch(1).await();
		} catch (InterruptedException e) {
		}
	}
	
}
