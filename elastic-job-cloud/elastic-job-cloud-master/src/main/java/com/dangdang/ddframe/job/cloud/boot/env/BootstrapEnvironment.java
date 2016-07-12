/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.boot.env;

import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 启动环境对象.
 *
 * @author zhangliang
 */
@Slf4j
public final class BootstrapEnvironment {
    
    private static final String PROPERTIES_PATH = "conf/elastic-job-cloud.properties";
    
    private final Properties properties;
    
    public BootstrapEnvironment() throws IOException {
        properties = getProperties();
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES_PATH)) {
            result.load(fileInputStream);
        } catch (final IOException ex) {
            log.warn("Cannot found conf/elastic-job-cloud.properties, use default value now.");
        }
        return result;
    }
    
    /**
     * 获取Zookeeper配置对象.
     * 
     * @return Zookeeper配置对象
     */
    // TODO 其他zkConfig的值可配置
    public ZookeeperConfiguration getZookeeperConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration(getValue(EnvironmentArgument.ZOOKEEPER_SERVERS), getValue(EnvironmentArgument.ZOOKEEPER_NAMESPACE));
        String digest = getValue(EnvironmentArgument.ZOOKEEPER_DIGEST);
        if (!Strings.isNullOrEmpty(digest)) {
            result.setDigest(digest);
        }
        return result;
    }
    
    /**
     * 获取Mesos配置对象.
     * 
     * @return Mesos配置对象
     */
    public MesosConfiguration getMesosConfiguration() {
        return new MesosConfiguration(getValue(EnvironmentArgument.USERNAME), getValue(EnvironmentArgument.MESOS_URL), getValue(EnvironmentArgument.HOSTNAME));
    }
    
    /**
     * 获取Restful服务器配置对象.
     *
     * @return Restful服务器配置对象
     */
    public RestfulServerConfiguration getRestfulServerConfiguration() {
        return new RestfulServerConfiguration(Integer.parseInt(getValue(EnvironmentArgument.PORT)));
    }
    
    // TODO 检查必填参数
    private String getValue(final EnvironmentArgument environmentArgument) {
        return properties.getProperty(environmentArgument.getKey(), environmentArgument.getDefaultValue());
    }
    
    @RequiredArgsConstructor
    @Getter
    enum EnvironmentArgument {
        
        ZOOKEEPER_SERVERS("zk_servers", "localhost:2181"),
        
        ZOOKEEPER_NAMESPACE("zk_namespace", "elastic-job-cloud"),
        
        ZOOKEEPER_DIGEST("zk_digest", ""),
        
        USERNAME("username", ""),
        
        HOSTNAME("hostname", ""),
        
        MESOS_URL("mesos_url", "zk://localhost:2181/mesos"),
        
        PORT("http_port", "8899");
        
        private final String key;
        
        private final String defaultValue;
    }
}
