/*
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

package com.dangdang.ddframe.job.state;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * 带有UUID唯一序号的作业.
 *
 * @author zhangliang
 */
@Getter
public final class UniqueJob {
    
    private static final String DELIMITER = "@-@";
    
    private final String jobName;
    
    private final String uniqueName;
    
    public UniqueJob(final String jobName) {
        this.jobName = jobName;
        uniqueName = Joiner.on(DELIMITER).join(jobName, UUID.randomUUID().toString());
    }
    
    public UniqueJob(final String jobName, final String uuid) {
        this.jobName = jobName;
        uniqueName = Joiner.on(DELIMITER).join(jobName, uuid);
    }
    
    /**
     * 根据作业唯一序号生成对象.
     * 
     * @param uniqueName 作业唯一序号
     * @return 带有UUID唯一序号的作业对象
     */
    public static UniqueJob from(final String uniqueName) {
        List<String> target = Splitter.on(DELIMITER).splitToList(uniqueName);
        return new UniqueJob(target.get(0), target.get(1));
    }
}
