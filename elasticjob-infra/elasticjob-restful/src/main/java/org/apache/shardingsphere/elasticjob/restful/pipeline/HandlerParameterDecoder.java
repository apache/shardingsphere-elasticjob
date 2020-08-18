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

package org.apache.shardingsphere.elasticjob.restful.pipeline;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.Handler;
import org.apache.shardingsphere.elasticjob.restful.HandlerParameter;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.MappingContext;
import org.apache.shardingsphere.elasticjob.restful.PathMatcher;
import org.apache.shardingsphere.elasticjob.restful.RegexPathMatcher;
import org.apache.shardingsphere.elasticjob.restful.deserializer.RequestBodyDeserializer;
import org.apache.shardingsphere.elasticjob.restful.deserializer.RequestBodyDeserializerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This handler is used for preparing parameters before executing handle method.
 * It prepares arguments declared by {@link org.apache.shardingsphere.elasticjob.restful.annotation.Param}
 * and {@link org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody}, and deserializes arguments to declared type.
 */
@Slf4j
@Sharable
public class HandlerParameterDecoder extends ChannelInboundHandlerAdapter {
    
    private final PathMatcher pathMatcher = new RegexPathMatcher();
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        HandleContext<Handler> handleContext = (HandleContext<Handler>) msg;
        FullHttpRequest httpRequest = handleContext.getHttpRequest();
        MappingContext<Handler> mappingContext = handleContext.getMappingContext();
        Object[] arguments = prepareArguments(httpRequest, mappingContext);
        handleContext.setArgs(arguments);
        ctx.fireChannelRead(handleContext);
    }
    
    private Object[] prepareArguments(final FullHttpRequest httpRequest, final MappingContext<Handler> mappingContext) {
        Handler handler = mappingContext.payload();
        List<HandlerParameter> handlerParameters = handler.getHandlerParameters();
        Map<String, List<String>> queryParameters = parseQuery(httpRequest.uri());
        Map<String, String> templateVariables = pathMatcher.captureVariables(mappingContext.pattern(), httpRequest.uri());
        Object[] args = new Object[handlerParameters.size()];
        boolean requestBodyAlreadyParsed = false;
        for (int i = 0; i < handlerParameters.size(); i++) {
            HandlerParameter handlerParameter = handlerParameters.get(i);
            Object parsedValue = null;
            switch (handlerParameter.getParamSource()) {
                case PATH:
                    parsedValue = deserializeBuiltInType(handlerParameter.getType(), templateVariables.get(handlerParameter.getName()));
                    break;
                case QUERY:
                    List<String> queryValues = queryParameters.get(handlerParameter.getName());
                    parsedValue = deserializeQueryParameter(handlerParameter.getType(), queryValues);
                    break;
                case HEADER:
                    String headerValue = httpRequest.headers().get(handlerParameter.getName());
                    parsedValue = deserializeBuiltInType(handlerParameter.getType(), headerValue);
                    break;
                case BODY:
                    Preconditions.checkState(!requestBodyAlreadyParsed, "@RequestBody duplicated on handle method.");
                    byte[] bytes = ByteBufUtil.getBytes(httpRequest.content());
                    String mimeType = Optional.ofNullable(HttpUtil.getMimeType(httpRequest))
                            .orElseGet(() -> HttpUtil.getMimeType(Http.DEFAULT_CONTENT_TYPE)).toString();
                    RequestBodyDeserializer deserializer = RequestBodyDeserializerFactory.getRequestBodyDeserializer(mimeType);
                    if (null == deserializer) {
                        throw new UnsupportedMessageTypeException(MessageFormat.format("Unsupported MIME type [{0}]", mimeType));
                    }
                    parsedValue = deserializer.deserialize(handlerParameter.getType(), bytes);
                    requestBodyAlreadyParsed = true;
                    break;
                case UNKNOWN:
                    log.warn("Unknown source argument [{}] on index [{}].", handlerParameter.getName(), handlerParameter.getIndex());
                    break;
                default:
            }
            args[i] = parsedValue;
        }
        return args;
    }
    
    private Map<String, List<String>> parseQuery(final String uri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        return queryStringDecoder.parameters();
    }
    
    private Object deserializeQueryParameter(final Class<?> targetType, final List<String> queryValues) {
        if (null == queryValues || queryValues.isEmpty()) {
            return null;
        }
        if (1 == queryValues.size()) {
            return deserializeBuiltInType(targetType, queryValues.get(0));
        }
        throw new UnsupportedOperationException("Multi value query doesn't support yet.");
    }
    
    private Object deserializeBuiltInType(final Class<?> targetType, final String value) {
        Preconditions.checkArgument(!value.isEmpty(), "Cannot deserialize empty value.");
        if (String.class.equals(targetType)) {
            return value;
        }
        if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
            return Boolean.parseBoolean(value);
        }
        if (Character.class.equals(targetType) || char.class.equals(targetType)) {
            Preconditions.checkArgument(1 >= value.length(), MessageFormat.format("Cannot set value [{0}] into a char.", value));
            return value.charAt(0);
        }
        if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
            return Byte.parseByte(value);
        }
        if (Short.class.equals(targetType) || short.class.equals(targetType)) {
            return Short.parseShort(value);
        }
        if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
            return Integer.parseInt(value);
        }
        if (Long.class.equals(targetType) || long.class.equals(targetType)) {
            return Long.parseLong(value);
        }
        if (Float.class.equals(targetType) || float.class.equals(targetType)) {
            return Float.parseFloat(value);
        }
        if (Double.class.equals(targetType) || double.class.equals(targetType)) {
            return Double.parseDouble(value);
        }
        throw new IllegalArgumentException(MessageFormat.format("Cannot deserialize path variable [{0}] into [{1}]", value, targetType.getName()));
    }
}
