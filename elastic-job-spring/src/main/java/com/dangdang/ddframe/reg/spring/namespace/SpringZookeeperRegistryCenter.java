/**
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

package com.dangdang.ddframe.reg.spring.namespace;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.dangdang.ddframe.reg.spring.placeholder.PlaceholderResolved;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Strings;

/**
 * 使用Spring启动基于Zookeeper的注册中心.
 * 
 * @author zhangliang
 */
public final class SpringZookeeperRegistryCenter extends ZookeeperRegistryCenter implements BeanFactoryPostProcessor {
    
    private final SpringZookeeperConfigurationDto springZookeeperConfigurationDto;
    
    public SpringZookeeperRegistryCenter(final SpringZookeeperConfigurationDto springZookeeperConfigurationDto) {
        super(new ZookeeperConfiguration());
        this.springZookeeperConfigurationDto = springZookeeperConfigurationDto;
    }
    
    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
        PlaceholderResolved placeholderResolved = new PlaceholderResolved(beanFactory);
        getZkConfig().setServerLists(placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getServerLists()));
        getZkConfig().setNamespace(placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getNamespace()));
        getZkConfig().setBaseSleepTimeMilliseconds(Integer.parseInt(placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getBaseSleepTimeMilliseconds())));
        getZkConfig().setMaxSleepTimeMilliseconds(Integer.parseInt(placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getMaxSleepTimeMilliseconds())));
        getZkConfig().setMaxRetries(Integer.parseInt(placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getMaxRetries())));
        String sessionTimeoutMilliseconds = placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getSessionTimeoutMilliseconds());
        if (!Strings.isNullOrEmpty(sessionTimeoutMilliseconds)) {
            getZkConfig().setSessionTimeoutMilliseconds(Integer.parseInt(sessionTimeoutMilliseconds));
        }
        String connectionTimeoutMilliseconds = placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getConnectionTimeoutMilliseconds());
        if (!Strings.isNullOrEmpty(connectionTimeoutMilliseconds)) {
            getZkConfig().setConnectionTimeoutMilliseconds(Integer.parseInt(connectionTimeoutMilliseconds));
        }
        String digest = placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getDigest());
        if (!Strings.isNullOrEmpty(digest)) {
            getZkConfig().setDigest(digest);
        }
        String localPropertiesPath = placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getLocalPropertiesPath());
        if (!Strings.isNullOrEmpty(localPropertiesPath)) {
            getZkConfig().setLocalPropertiesPath(localPropertiesPath);
        }
        String overwrite = placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getOverwrite());
        if (!Strings.isNullOrEmpty(overwrite)) {
            getZkConfig().setOverwrite(Boolean.valueOf(overwrite));
        }
        init();
    }
}
