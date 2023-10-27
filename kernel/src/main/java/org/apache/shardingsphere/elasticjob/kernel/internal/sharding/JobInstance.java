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

package org.apache.shardingsphere.elasticjob.kernel.internal.sharding;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * Job instance.
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "jobInstanceId")
public final class JobInstance {
    
    public static final String DELIMITER = "@-@";
    
    private String jobInstanceId;
    
    private String labels;
    
    private String serverIp;
    
    public JobInstance() {
        this(IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }
    
    public JobInstance(final String jobInstanceId) {
        this(jobInstanceId, null);
    }
    
    public JobInstance(final String jobInstanceId, final String labels) {
        this(jobInstanceId, labels, IpUtils.getIp());
    }
}
