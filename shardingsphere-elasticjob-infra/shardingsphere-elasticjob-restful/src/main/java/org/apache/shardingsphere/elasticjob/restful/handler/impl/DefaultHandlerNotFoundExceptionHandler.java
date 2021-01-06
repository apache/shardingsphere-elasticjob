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

package org.apache.shardingsphere.elasticjob.restful.handler.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandleResult;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandler;
import org.apache.shardingsphere.elasticjob.restful.handler.HandlerNotFoundException;

/**
 * A default handler for {@link HandlerNotFoundException}.
 */
public final class DefaultHandlerNotFoundExceptionHandler implements ExceptionHandler<HandlerNotFoundException> {
    
    @Override
    public ExceptionHandleResult handleException(final HandlerNotFoundException ex) {
        return ExceptionHandleResult.builder()
                .statusCode(HttpResponseStatus.NOT_FOUND.code())
                .result(ex.getLocalizedMessage())
                .contentType(Http.DEFAULT_CONTENT_TYPE)
                .build();
    }
}
