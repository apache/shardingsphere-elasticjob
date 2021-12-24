/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.env;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

/**
 * Bootstrap env.
 */
@Slf4j
public final class BootstrapEnvironment {
    
    @Getter
    private static final BootstrapEnvironment INSTANCE = new BootstrapEnvironment();
    
    private static final String PROPERTIES_PATH = "conf/elasticjob-cloud-scheduler.properties";
    
    private final Properties properties;
    
    private BootstrapEnvironment() {
        properties = getProperties();
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        try (InputStream fileInputStream = BootstrapEnvironment.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            result.load(fileInputStream);
        } catch (final IOException ex) {
            log.warn("Can not load properties file from path: '{}'.", PROPERTIES_PATH);
        }
        setPropertiesByEnv(result);
        return result;
    }
    
    private void setPropertiesByEnv(final Properties prop) {
        for (EnvironmentArgument each : EnvironmentArgument.values()) {
            String key = each.getKey();
            String value = System.getenv(key);
            if (!Strings.isNullOrEmpty(value)) {
                log.info("Load property {} with value {} from ENV.", key, value);
                prop.setProperty(each.getKey(), value);
            }
        }
    }
    
    /**
     * Get the host and port of the framework.
     *
     * @return host and port of the framework
     */
    public String getFrameworkHostPort() {
        return String.format("%s:%d", getMesosConfiguration().getHostname(), getRestfulServerConfiguration().getPort());
    }
    
    /**
     * Get mesos config.
     *
     * @return mesos config
     */
    public MesosConfiguration getMesosConfiguration() {
        return new MesosConfiguration(getValue(EnvironmentArgument.USER), getValue(EnvironmentArgument.MESOS_URL), getValue(EnvironmentArgument.HOSTNAME));
    }
    
    /**
     * Get zookeeper configuration.
     *
     * @return zookeeper configuration
     */
    // TODO Other zkConfig values are configurable
    public ZookeeperConfiguration getZookeeperConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration(getValue(EnvironmentArgument.ZOOKEEPER_SERVERS), getValue(EnvironmentArgument.ZOOKEEPER_NAMESPACE));
        String digest = getValue(EnvironmentArgument.ZOOKEEPER_DIGEST);
        if (!Strings.isNullOrEmpty(digest)) {
            result.setDigest(digest);
        }
        return result;
    }
    
    /**
     * Get restful server config.
     *
     * @return restful server config
     */
    public RestfulServerConfiguration getRestfulServerConfiguration() {
        return new RestfulServerConfiguration(Integer.parseInt(getValue(EnvironmentArgument.PORT)));
    }
    
    /**
     * Get framework config.
     *
     * @return the framework config
     */
    public FrameworkConfiguration getFrameworkConfiguration() {
        return new FrameworkConfiguration(Integer.parseInt(getValue(EnvironmentArgument.JOB_STATE_QUEUE_SIZE)), Integer.parseInt(getValue(EnvironmentArgument.RECONCILE_INTERVAL_MINUTES)));
    }

    /**
     * Get user auth config.
     *
     * @return the user auth config.
     */
    public AuthConfiguration getUserAuthConfiguration() {
        return new AuthConfiguration(getValue(EnvironmentArgument.AUTH_USERNAME), getValue(EnvironmentArgument.AUTH_PASSWORD));
    }
    
    /**
     * Get tracing configuration.
     *
     * @return tracing configuration
     */
    public Optional<TracingConfiguration<?>> getTracingConfiguration() {
        String driver = getValue(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER);
        String url = getValue(EnvironmentArgument.EVENT_TRACE_RDB_URL);
        String username = getValue(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME);
        String password = getValue(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD);
        if (!Strings.isNullOrEmpty(driver) && !Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(username)) {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driver);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            return Optional.of(new TracingConfiguration<DataSource>("RDB", dataSource));
        }
        return Optional.empty();
    }
    
    /**
     * Get job event rdb config map.
     *
     * @return map of the rdb config
     */
    // CHECKSTYLE:OFF
    public HashMap<String, String> getJobEventRdbConfigurationMap() {
        HashMap<String, String> result = new HashMap<>(4, 1);
        // CHECKSTYLE:ON
        result.put(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey(), getValue(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER));
        result.put(EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey(), getValue(EnvironmentArgument.EVENT_TRACE_RDB_URL));
        result.put(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey(), getValue(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME));
        result.put(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey(), getValue(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD));
        return result;
    }
    
    /**
     * Get the role of the mesos.
     *
     * @return the role.
     */
    public Optional<String> getMesosRole() {
        String role = getValue(EnvironmentArgument.MESOS_ROLE);
        if (Strings.isNullOrEmpty(role)) {
            return Optional.empty();
        }
        return Optional.of(role);
    }
    
    private String getValue(final EnvironmentArgument environmentArgument) {
        String result = properties.getProperty(environmentArgument.getKey(), environmentArgument.getDefaultValue());
        if (environmentArgument.isRequired()) {
            Preconditions.checkState(!Strings.isNullOrEmpty(result), String.format("Property `%s` is required.", environmentArgument.getKey()));
        }
        return result;
    }
    
    /**
     * Env args.
     */
    @RequiredArgsConstructor
    @Getter
    public enum EnvironmentArgument {
        
        HOSTNAME("hostname", "localhost", true),
        
        MESOS_URL("mesos_url", "zk://localhost:2181/mesos", true),
        
        MESOS_ROLE("mesos_role", "", false),
        
        USER("user", "", false),
        
        ZOOKEEPER_SERVERS("zk_servers", "localhost:2181", true),
        
        ZOOKEEPER_NAMESPACE("zk_namespace", "elasticjob-cloud", true),
        
        ZOOKEEPER_DIGEST("zk_digest", "", false),
        
        PORT("http_port", "8899", true),
        
        JOB_STATE_QUEUE_SIZE("job_state_queue_size", "10000", true),
        
        EVENT_TRACE_RDB_DRIVER("event_trace_rdb_driver", "", false),

        EVENT_TRACE_RDB_URL("event_trace_rdb_url", "", false),

        EVENT_TRACE_RDB_USERNAME("event_trace_rdb_username", "", false),

        EVENT_TRACE_RDB_PASSWORD("event_trace_rdb_password", "", false),
    
        RECONCILE_INTERVAL_MINUTES("reconcile_interval_minutes", "-1", false),

        AUTH_USERNAME("auth_username", "root", true),

        AUTH_PASSWORD("auth_password", "pwd", true);
        
        private final String key;
        
        private final String defaultValue;
        
        private final boolean required;
    }
}
