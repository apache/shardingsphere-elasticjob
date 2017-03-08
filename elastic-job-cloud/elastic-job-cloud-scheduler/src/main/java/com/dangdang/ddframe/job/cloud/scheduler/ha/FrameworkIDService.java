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

package com.dangdang.ddframe.job.cloud.scheduler.ha;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;

/**
 * FrameworkID 的保存器.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class FrameworkIDService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * 获取FrameworkID,返回值是一个可选的结果.
     * 
     * @return 获取FrameworkID的可选结果
     */
    public Optional<String> fetch() {
        String frameworkId = regCenter.getDirectly(HANode.FRAMEWORK_ID_NODE);
        return Strings.isNullOrEmpty(frameworkId) ? Optional.<String>absent() : Optional.of(frameworkId);
    }
    
    /**
     * 保存FrameworkID.
     * 
     * @param id Framework的ID
     */
    public void save(final String id) {
        if (!regCenter.isExisted(HANode.FRAMEWORK_ID_NODE)) {
            regCenter.persist(HANode.FRAMEWORK_ID_NODE, id);
        }
    }
}
