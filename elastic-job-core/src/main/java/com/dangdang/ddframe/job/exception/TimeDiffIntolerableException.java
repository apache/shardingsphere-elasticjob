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
 * 本机与注册中心的时间误差超过容忍范围抛出的异常.
 * 
 * @author zhangliang
 */
public final class TimeDiffIntolerableException extends JobException {
    
    private static final long serialVersionUID = -6287464997081326084L;
    
    private static final String ERROR_MSG = "Time different between job server and register center exceed [%s] seconds, max time different is [%s] seconds.";
    
    public TimeDiffIntolerableException(final int timeDiffSeconds, final int maxTimeDiffSeconds) {
        super(ERROR_MSG, timeDiffSeconds, maxTimeDiffSeconds);
    }
}
