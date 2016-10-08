/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.spring.placeholder;

import com.dangdang.ddframe.job.reg.base.RegistryCenter;
import com.dangdang.ddframe.job.reg.exception.RegException;
import org.springframework.core.env.PropertySource;

import java.util.UUID;

/**
 * 将注册中心的配置数据转化为Spring的属性源.
 * 
 * @author zhangliang
 */
public class RegistryPropertySource extends PropertySource<RegistryCenter> {
    
    private final RegistryCenter source;
    
    public RegistryPropertySource(final RegistryCenter source) {
        super(UUID.randomUUID().toString(), source);
        this.source = source;
    }
    
    @Override
    public Object getProperty(final String name) {
        try {
            return source.get(name);
        } catch (final RegException ex) {
            return null;
        }
    }
}
