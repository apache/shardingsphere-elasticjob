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

package com.dangdang.ddframe.job.lite.api.strategy;

import com.dangdang.ddframe.job.util.env.IpUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 作业运行实例.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "jobInstanceId")
public final class JobInstance {
    
    /**
     * 默认作业运行实例主键.
     */
    public static final String DEFAULT_INSTANCE_ID = "1.1.1.1@-@1";
    
    private static final String DELIMITER = "@-@";
    
    /**
     * 作业实例主键.
     */
    private final String jobInstanceId;
    
    public JobInstance() {
        jobInstanceId = IpUtils.getIp() + DELIMITER + UUID.randomUUID().toString();
    }
    
    /**
     * 获取作业服务器IP地址.
     * 
     * @return 作业服务器IP地址
     */
    public String getIp() {
        return jobInstanceId.substring(0, jobInstanceId.indexOf(DELIMITER));
    }
}
