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
import lombok.Setter;

@Getter
@Setter
public final class JobBriefInfo implements Serializable, Comparable<JobBriefInfo> {
    
    private static final long serialVersionUID = 8405751873086755148L;
    
    private String jobName;
    
    private JobStatus status;
    
    private String description;

    private String cron;
    
    @Override
    public int compareTo(final JobBriefInfo o) {
        return getJobName().compareTo(o.getJobName());
    }
    
    public enum JobStatus {
        OK, 
        PARTIAL_ALIVE, 
        MANUALLY_DISABLED, 
        ALL_CRASHED;
        
        public static JobStatus getJobStatus(final int okCount, final int crashedCount, final int manuallyDisabledCount, final int serverCount) {
            if (okCount == serverCount) {
                return JobStatus.OK;
            }
            if (crashedCount == serverCount) {
                return JobStatus.ALL_CRASHED;
            }
            if (crashedCount > 0) {
                return JobStatus.PARTIAL_ALIVE;
            }
            if (manuallyDisabledCount > 0) {
                return JobStatus.MANUALLY_DISABLED;
            }
            return JobStatus.OK;
        }
    }
}
