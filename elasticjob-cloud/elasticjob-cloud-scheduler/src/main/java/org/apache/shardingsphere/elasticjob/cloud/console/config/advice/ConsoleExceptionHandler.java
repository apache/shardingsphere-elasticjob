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

package org.apache.shardingsphere.elasticjob.cloud.console.config.advice;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandleResult;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandler;

/**
 * A default exception handler for restful service.
 **/
@Slf4j
public final class ConsoleExceptionHandler implements ExceptionHandler<Exception> {
    
    @Override
    public ExceptionHandleResult handleException(final Exception ex) {
        return ExceptionHandleResult.builder()
                .statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .result(ex.getLocalizedMessage())
                .build();
    }
}
