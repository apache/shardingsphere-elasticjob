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

package org.apache.shardingsphere.elasticjob.http.pojo;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobConfigurationException;

import java.util.Arrays;
import java.util.Properties;

/**
 * Http job param.
 */
@RequiredArgsConstructor
@Getter
public final class HttpParam {
    
    private final String url;
    
    private final String method;
    
    private final String contentType;
    
    private final String data;
    
    private final int connectTimeoutMilliseconds;
    
    private final int readTimeoutMilliseconds;
    
    public HttpParam(final Properties props) {
        url = props.getProperty(HttpJobProperties.URI_KEY);
        if (Strings.isNullOrEmpty(url)) {
            throw new JobConfigurationException("Cannot find HTTP URL, job is not executed.");
        }
        method = props.getProperty(HttpJobProperties.METHOD_KEY);
        if (Strings.isNullOrEmpty(method)) {
            throw new JobConfigurationException("Cannot find HTTP method, job is not executed.");
        }
        contentType = props.getProperty(HttpJobProperties.CONTENT_TYPE_KEY);
        data = props.getProperty(HttpJobProperties.DATA_KEY);
        connectTimeoutMilliseconds = Integer.parseInt(props.getProperty(HttpJobProperties.CONNECT_TIMEOUT_KEY, "3000"));
        readTimeoutMilliseconds = Integer.parseInt(props.getProperty(HttpJobProperties.READ_TIMEOUT_KEY, "5000"));
    }
    
    /**
     * Is write method.
     * 
     * @return write method or not
     */
    public boolean isWriteMethod() {
        return Arrays.asList("POST", "PUT", "DELETE").contains(method.toUpperCase());
    }
}
