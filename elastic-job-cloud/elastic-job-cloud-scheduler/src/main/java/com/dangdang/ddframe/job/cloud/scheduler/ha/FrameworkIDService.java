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
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;

/**
 * FrameworkID 的保存器.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class FrameworkIDService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * 向FrameworkInfo构建器提供FrameworkI值.
     * 
     * @param builder FrameworkInfo构建器
     * @return FrameworkInfo构建器
     */
    public Protos.FrameworkInfo.Builder supply(final Protos.FrameworkInfo.Builder builder) {
        String frameworkId = regCenter.getDirectly(FrameworkIDNode.FRAMEWORK_ID_NODE);
        if (!Strings.isNullOrEmpty(frameworkId)) {
            builder.setId(Protos.FrameworkID.newBuilder().setValue(frameworkId));
        }
        return builder;
    }
    
    /**
     * 保存FrameworkID.
     * 
     * @param id Framework的ID
     */
    public void save(final Protos.FrameworkID id) {
        if (!regCenter.isExisted(FrameworkIDNode.FRAMEWORK_ID_NODE)) {
            regCenter.persist(FrameworkIDNode.FRAMEWORK_ID_NODE, id.getValue());
        }
    }
}
