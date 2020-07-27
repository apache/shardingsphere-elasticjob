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

package org.apache.shardingsphere.elasticjob.cloud.console.config;

import org.apache.shardingsphere.elasticjob.cloud.console.security.UserAuthenticationService;
import org.apache.shardingsphere.elasticjob.cloud.console.security.WwwAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Filter register config.
 **/
@Configuration
public class FilterRegisterConfig {
    
    private final UserAuthenticationService userAuthenticationService;
    
    @Autowired
    public FilterRegisterConfig(final UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }
    
    /**
     * register www auth filter.
     *
     * @return www auth filter bean
     */
    @Bean
    public FilterRegistrationBean<WwwAuthFilter> wwwAuthFilter() {
        WwwAuthFilter wwwAuthFilter = new WwwAuthFilter();
        wwwAuthFilter.setUserAuthenticationService(userAuthenticationService);
        FilterRegistrationBean<WwwAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(wwwAuthFilter);
        registration.addUrlPatterns("/");
        registration.addUrlPatterns("*.html");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        return registration;
    }
}
