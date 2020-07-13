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

package org.apache.shardingsphere.elasticjob.lite.console.config.advice;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.exception.ExceptionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Console rest controller advice.
 **/
@RestControllerAdvice
@Slf4j
public final class ConsoleRestControllerAdvice implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        //only advice return void method.
        if (null == returnType.getMethod()) {
            return false;
        }
        return void.class.isAssignableFrom(returnType.getMethod().getReturnType());
    }
    
    @Override
    public Object beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType,
                                  final Class<? extends HttpMessageConverter<?>> selectedConverterType, final ServerHttpRequest request, final ServerHttpResponse response) {
        //if the method return void, then the value is true and returns.
        return null == body ? true : body;
    }
    
    /**
     * Handle exception.
     *
     * @param ex exception
     * @return response result
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> toResponse(final Exception ex) {
        log.error("CONSOLE ERROR", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionUtils.transform(ex));
    }
}
