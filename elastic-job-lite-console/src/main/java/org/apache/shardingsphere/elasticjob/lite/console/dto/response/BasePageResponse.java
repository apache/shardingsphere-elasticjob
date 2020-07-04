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

package org.apache.shardingsphere.elasticjob.lite.console.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasePageResponse<T> implements Serializable {
    
    /**
     * Total count of rows.
     */
    private Long total;
    
    /**
     * Rows data.
     */
    private List<T> rows;
    
    /**
     * Create new BasePageResponse with total and data.
     * @param total Total count of match data
     * @param data Current page of data
     * @param <T> Data type
     * @return BasePageResponse
     */
    public static <T> BasePageResponse of(final Long total, final List<T> data) {
        return new BasePageResponse(total, data);
    }
    
    /**
     * Create new BasePageResponse with Page.
     * @param page match data info.
     * @param <T> Data type
     * @return BasePageResponse
     */
    public static <T> BasePageResponse of(final Page<T> page) {
        return new BasePageResponse(page.getTotalElements(), page.getContent());
    }
}
