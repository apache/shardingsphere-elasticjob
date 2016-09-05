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

package com.dangdang.ddframe.job.lite.lifecycle.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 作业简明信息对象.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class JobBriefInfo implements Serializable, Comparable<JobBriefInfo> {
    
    private static final long serialVersionUID = 8405751873086755148L;
    
    private String jobName;
    
    private String jobType;
    
    private JobStatus status;
    
    private String description;

    private String cron;
    
    @Override
    public int compareTo(final JobBriefInfo o) {
        return getJobName().compareTo(o.getJobName());
    }
    
    /**
     * 作业状态.
     *
     * @author zhangliang
     */
    public enum JobStatus {
        
        OK, 
        PARTIAL_ALIVE,
        DISABLED, 
        ALL_CRASHED;
        
        /**
         * 获取作业状态.
         * 
         * @param okCount 作业成功总数
         * @param crashedCount 作业崩溃总数
         * @param disabledCount 作业禁用总数
         * @param serverCount 作业服务器总数
         * @return 作业状态
         */
        public static JobStatus getJobStatus(final int okCount, final int crashedCount, final int disabledCount, final int serverCount) {
            if (okCount == serverCount) {
                return OK;
            }
            if (crashedCount == serverCount) {
                return ALL_CRASHED;
            }
            if (crashedCount > 0) {
                return PARTIAL_ALIVE;
            }
            if (disabledCount > 0) {
                return DISABLED;
            }
            return OK;
        }
    }
}
