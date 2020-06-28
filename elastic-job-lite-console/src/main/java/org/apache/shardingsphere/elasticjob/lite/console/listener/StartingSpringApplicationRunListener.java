package org.apache.shardingsphere.elasticjob.lite.console.listener;

import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Starting elastic job console application run listener.
 **/
public class StartingSpringApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private static final String LOCAL_IP_PROPERTY_KEY = "localIp";

    public StartingSpringApplicationRunListener(final SpringApplication application, final String[] args) {
    }

    @Override
    public void environmentPrepared(final ConfigurableEnvironment environment) {
        System.setProperty(LOCAL_IP_PROPERTY_KEY, IpUtils.getIp());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
