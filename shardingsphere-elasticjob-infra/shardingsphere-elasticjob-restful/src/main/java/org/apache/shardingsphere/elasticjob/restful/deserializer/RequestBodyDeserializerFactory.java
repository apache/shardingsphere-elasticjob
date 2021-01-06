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

package org.apache.shardingsphere.elasticjob.restful.deserializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.restful.deserializer.factory.DeserializerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Request body deserializer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestBodyDeserializerFactory {
    
    private static final Map<String, RequestBodyDeserializer> REQUEST_BODY_DESERIALIZERS = new ConcurrentHashMap<>();
    
    private static final Map<String, DeserializerFactory> DEFAULT_REQUEST_BODY_DESERIALIZER_FACTORIES = new ConcurrentHashMap<>();
    
    private static final RequestBodyDeserializer MISSING_DESERIALIZER = new RequestBodyDeserializer() {
        @Override
        public String mimeType() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <T> T deserialize(final Class<T> targetType, final byte[] requestBodyBytes) {
            throw new UnsupportedOperationException();
        }
    };
    
    static {
        for (RequestBodyDeserializer deserializer : ServiceLoader.load(RequestBodyDeserializer.class)) {
            REQUEST_BODY_DESERIALIZERS.put(deserializer.mimeType(), deserializer);
        }
        for (DeserializerFactory factory : ServiceLoader.load(DeserializerFactory.class)) {
            DEFAULT_REQUEST_BODY_DESERIALIZER_FACTORIES.put(factory.mimeType(), factory);
        }
    }
    
    /**
     * Get deserializer for specific HTTP content type.
     *
     * <p>
     * This method will look for a deserializer instance of specific MIME type.
     * If deserializer not found, this method would look for deserializer factory by MIME type.
     * If it is still not found, the MIME type would be marked as <code>MISSING_DESERIALIZER</code>.
     * </p>
     *
     * <p>
     * Some default deserializer will be provided by {@link DeserializerFactory},
     * so developers can implement {@link RequestBodyDeserializer} and register it by SPI to override default deserializer.
     * </p>
     *
     * @param contentType HTTP content type
     * @return Deserializer
     */
    public static RequestBodyDeserializer getRequestBodyDeserializer(final String contentType) {
        RequestBodyDeserializer result = REQUEST_BODY_DESERIALIZERS.get(contentType);
        if (null == result) {
            synchronized (RequestBodyDeserializerFactory.class) {
                if (null == REQUEST_BODY_DESERIALIZERS.get(contentType)) {
                    instantiateRequestBodyDeserializerFromFactories(contentType);
                }
                result = REQUEST_BODY_DESERIALIZERS.get(contentType);
            }
        }
        if (MISSING_DESERIALIZER == result) {
            throw new RequestBodyDeserializerNotFoundException(contentType);
        }
        return result;
    }
    
    private static void instantiateRequestBodyDeserializerFromFactories(final String contentType) {
        RequestBodyDeserializer deserializer;
        DeserializerFactory factory = DEFAULT_REQUEST_BODY_DESERIALIZER_FACTORIES.get(contentType);
        deserializer = Optional.ofNullable(factory).map(DeserializerFactory::createDeserializer).orElse(MISSING_DESERIALIZER);
        REQUEST_BODY_DESERIALIZERS.put(contentType, deserializer);
    }
}
