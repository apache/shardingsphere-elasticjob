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

package org.apache.shardingsphere.elasticjob.restful.handler;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;
import org.apache.shardingsphere.elasticjob.restful.annotation.Returning;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Handle holds a handle method and an instance for method invoking.
 * Describes parameters requirements of handle method.
 */
public final class Handler {
    
    private final Object instance;
    
    private final Method handleMethod;
    
    @Getter
    private final List<HandlerParameter> handlerParameters;
    
    /**
     * HTTP status code to return.
     */
    @Getter
    private final int httpStatusCode;
    
    /**
     * Content type to producing.
     */
    @Getter
    private final String producing;
    
    public Handler(final Object instance, final Method handleMethod) {
        this.instance = instance;
        this.handleMethod = handleMethod;
        this.handlerParameters = parseHandleMethodParameter();
        this.httpStatusCode = parseReturning();
        this.producing = parseProducing();
    }
    
    /**
     * Execute handle method with required arguments.
     *
     * @param args Required arguments
     * @return Method invoke result
     * @throws InvocationTargetException Wraps exception thrown by invoked method
     * @throws IllegalAccessException    Handle method is not accessible
     */
    public Object execute(final Object... args) throws InvocationTargetException, IllegalAccessException {
        return handleMethod.invoke(instance, args);
    }
    
    private List<HandlerParameter> parseHandleMethodParameter() {
        List<HandlerParameter> params = new LinkedList<>();
        Parameter[] parameters = handleMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param annotation = parameter.getAnnotation(Param.class);
            HandlerParameter handlerParameter;
            RequestBody requestBody;
            if (null != annotation) {
                handlerParameter = new HandlerParameter(i, parameter.getType(), annotation.source(), annotation.name(), annotation.required());
            } else if (null != (requestBody = parameter.getAnnotation(RequestBody.class))) {
                handlerParameter = new HandlerParameter(i, parameter.getType(), ParamSource.BODY, parameter.getName(), requestBody.required());
            } else {
                handlerParameter = new HandlerParameter(i, parameter.getType(), ParamSource.UNKNOWN, parameter.getName(), false);
            }
            params.add(handlerParameter);
        }
        return Collections.unmodifiableList(params);
    }
    
    private int parseReturning() {
        Returning returning = handleMethod.getAnnotation(Returning.class);
        return Optional.ofNullable(returning).map(Returning::code).orElse(200);
    }
    
    private String parseProducing() {
        Returning returning = handleMethod.getAnnotation(Returning.class);
        return Optional.ofNullable(returning).map(Returning::contentType).orElse(Http.DEFAULT_CONTENT_TYPE);
    }
}
