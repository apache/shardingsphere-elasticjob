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

package com.dangdang.ddframe.job.console.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.curator.framework.CuratorFramework;

@Getter
@Setter
@NoArgsConstructor
public final class RegistryCenterClient implements Serializable {
    
    private static final long serialVersionUID = -946258964014121184L;
    
    private String name;
    
    private CuratorFramework curatorClient;
    
    private boolean connected;
    
    public RegistryCenterClient(final String name) {
        this.name = name;
    }
}
