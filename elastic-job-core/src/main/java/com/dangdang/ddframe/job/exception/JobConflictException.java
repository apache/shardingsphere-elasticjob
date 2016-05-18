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

package com.dangdang.ddframe.job.exception;

/**
 * 作业冲突抛出的异常.
 * 
 * <p>
 * 作业冲突的场景是作业名称一样, 但是作业的实现类不同.
 * 将停止作业注册抛出异常.
 * </p>
 * 
 * @author zhangliang
 */
public final class JobConflictException extends JobException {
    
    private static final long serialVersionUID = 8248963542503963491L;
    
    private static final String ERROR_MSG = "Job conflict with register center. The job [%s] in register center's class is [%s], your job class is [%s]";
    
    public JobConflictException(final String jobName, final String registeredJobClassName, final String toBeRegisteredJobClassName) {
        super(ERROR_MSG, jobName, registeredJobClassName, toBeRegisteredJobClassName);
    }
}
