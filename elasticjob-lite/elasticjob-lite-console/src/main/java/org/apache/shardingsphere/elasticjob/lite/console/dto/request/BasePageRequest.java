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

package org.apache.shardingsphere.elasticjob.lite.console.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pageable request base request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasePageRequest {
    
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * Page size of request.
     */
    @JsonProperty("per_page")
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    
    /**
     * Page number of request.
     */
    @JsonProperty("page")
    private Integer pageNumber = 1;
    
    /**
     * The field name for sort by.
     */
    @JsonProperty("sort")
    private String sortBy;
    
    /**
     * Order type, asc or desc.
     */
    @JsonProperty("order")
    private String orderType;
}
