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

package org.apache.shardingsphere.elasticjob.restful.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.ContextPath;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;
import org.apache.shardingsphere.elasticjob.restful.annotation.Returning;
import org.apache.shardingsphere.elasticjob.restful.pojo.JobPojo;

@Slf4j
@ContextPath("/job")
public final class JobController implements RestfulController {
    
    /**
     * Pretend to create a job.
     *
     * @param group group
     * @param jobName job name
     * @param cron job cron
     * @param description job description
     * @return result
     */
    @Mapping(method = Http.POST, path = "/{group}/{jobName}")
    public JobPojo createJob(@Param(name = "group", source = ParamSource.PATH) final String group,
                             @Param(name = "jobName", source = ParamSource.PATH) final String jobName,
                             @Param(name = "cron", source = ParamSource.QUERY) final String cron,
                             @RequestBody final String description) {
        JobPojo result = new JobPojo();
        result.setName(jobName);
        result.setCron(cron);
        result.setGroup(group);
        result.setDescription(description);
        return result;
    }
    
    /**
     * Throw an illegal state exception.
     *
     * @param message Exception message
     * @return None
     */
    @Mapping(method = Http.GET, path = "/throw/IllegalState")
    public Object throwIllegalStateException(@Param(name = "Exception-Message", source = ParamSource.HEADER) final String message) {
        throw new IllegalStateException(message);
    }
    
    /**
     * Throw an illegal argument exception.
     *
     * @param message exception message
     * @return none
     */
    @Mapping(method = Http.GET, path = "/throw/IllegalArgument")
    public Object throwIllegalArgumentException(@Param(name = "Exception-Message", source = ParamSource.HEADER) final String message) {
        throw new IllegalArgumentException(message);
    }
    
    /**
     * Return 204.
     *
     * @param noop useless
     * @return none
     */
    @Mapping(method = Http.GET, path = "/code/204")
    @Returning(code = 204)
    public Object return204(final String noop) {
        return null;
    }
}
