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

package org.apache.shardingsphere.elasticjob.restful.serializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.restful.serializer.factory.SerializerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Response body serializer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseBodySerializerFactory {
    
    private static final Map<String, ResponseBodySerializer> RESPONSE_BODY_SERIALIZERS = new ConcurrentHashMap<>();
    
    private static final Map<String, SerializerFactory> RESPONSE_BODY_SERIALIZER_FACTORIES = new ConcurrentHashMap<>();
    
    private static final ResponseBodySerializer MISSING_SERIALIZER = new ResponseBodySerializer() {
        @Override
        public String mimeType() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public byte[] serialize(final Object responseBody) {
            throw new UnsupportedOperationException();
        }
    };
    
    static {
        for (ResponseBodySerializer serializer : ServiceLoader.load(ResponseBodySerializer.class)) {
            RESPONSE_BODY_SERIALIZERS.put(serializer.mimeType(), serializer);
        }
        for (SerializerFactory factory : ServiceLoader.load(SerializerFactory.class)) {
            RESPONSE_BODY_SERIALIZER_FACTORIES.put(factory.mimeType(), factory);
        }
    }
    
    /**
     * Get serializer for specific HTTP content type.
     *
     * <p>
     * This method will look for a serializer instance of specific MIME type.
     * If serializer not found, this method would look for serializer factory by MIME type.
     * If it is still not found, the MIME type would be marked as <code>MISSING_SERIALIZER</code>.
     * </p>
     *
     * <p>
     * Some default serializer will be provided by {@link SerializerFactory},
     * so developers can implement {@link ResponseBodySerializer} and register it by SPI to override default serializer.
     * </p>
     *
     * @param contentType HTTP content type
     * @return serializer
     */
    public static ResponseBodySerializer getResponseBodySerializer(final String contentType) {
        ResponseBodySerializer result = RESPONSE_BODY_SERIALIZERS.get(contentType);
        if (null == result) {
            synchronized (ResponseBodySerializerFactory.class) {
                if (null == RESPONSE_BODY_SERIALIZERS.get(contentType)) {
                    instantiateResponseBodySerializerFromFactories(contentType);
                }
                result = RESPONSE_BODY_SERIALIZERS.get(contentType);
            }
        }
        if (MISSING_SERIALIZER == result) {
            throw new ResponseBodySerializerNotFoundException(contentType);
        }
        return result;
    }
    
    private static void instantiateResponseBodySerializerFromFactories(final String contentType) {
        ResponseBodySerializer serializer;
        SerializerFactory factory = RESPONSE_BODY_SERIALIZER_FACTORIES.get(contentType);
        serializer = Optional.ofNullable(factory).map(SerializerFactory::createSerializer).orElse(MISSING_SERIALIZER);
        RESPONSE_BODY_SERIALIZERS.put(contentType, serializer);
    }
}
