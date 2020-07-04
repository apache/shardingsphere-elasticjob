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

package org.apache.shardingsphere.elasticjob.lite.console.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Job register statistics.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "JOB_REGISTER_STATISTICS")
public final class JobRegisterStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "registered_count", length = 11)
    private Integer registeredCount;
    
    @Column(name = "statistics_time", nullable = false)
    private Date statisticsTime;
    
    @Column(name = "creation_time", nullable = false)
    private Date creationTime = new Date();
    
    public JobRegisterStatistics(final Integer registeredCount, final Date statisticsTime) {
        this.registeredCount = registeredCount;
        this.statisticsTime = statisticsTime;
    }
}
