package com.cxytiandi.elasticjob.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import com.cxytiandi.elasticjob.autoconfigure.JobParserAutoConfiguration;

/**
 * ElasticJob 开启注解
 * 
 * <p>在Spring Boot 启动类上加此注解开启自动配置<p>
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({JobParserAutoConfiguration.class})
public @interface EnableElasticJob {

}