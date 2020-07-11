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

package org.apache.shardingsphere.elasticjob.lite.console.service.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.JobExecutionLogRepository;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.JobStatusTraceLogRepository;
import org.apache.shardingsphere.elasticjob.lite.console.domain.JobExecutionLog;
import org.apache.shardingsphere.elasticjob.lite.console.domain.JobStatusTraceLog;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.BasePageRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobExecutionEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobStatusTraceEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceHistoryService;
import org.apache.shardingsphere.elasticjob.lite.console.util.BeanUtils;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event trace history service implementation.
 */
@Slf4j
@Component
public final class EventTraceHistoryServiceImpl implements EventTraceHistoryService {
    
    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;
    
    @Autowired
    private JobStatusTraceLogRepository jobStatusTraceLogRepository;
    
    @Override
    public Page<JobExecutionEvent> findJobExecutionEvents(final FindJobExecutionEventsRequest findJobExecutionEventsRequest) {
        Example<JobExecutionLog> jobExecutionLogExample = getExample(findJobExecutionEventsRequest, JobExecutionLog.class);
        Specification<JobExecutionLog> specification = getSpecWithExampleAndDate(jobExecutionLogExample, findJobExecutionEventsRequest.getStart(),
            findJobExecutionEventsRequest.getEnd(), "startTime");

        Page<JobExecutionLog> page = jobExecutionLogRepository.findAll(specification, getPageable(findJobExecutionEventsRequest, JobExecutionLog.class));
        return new PageImpl<>(page.get().map(JobExecutionLog::toJobExecutionEvent).collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }
    
    @Override
    public Page<JobStatusTraceEvent> findJobStatusTraceEvents(final FindJobStatusTraceEventsRequest findJobStatusTraceEventsRequest) {
        Example<JobStatusTraceLog> jobStatusTraceLogExample = getExample(findJobStatusTraceEventsRequest, JobStatusTraceLog.class);
        Specification<JobStatusTraceLog> specification = getSpecWithExampleAndDate(jobStatusTraceLogExample, findJobStatusTraceEventsRequest.getStart(),
            findJobStatusTraceEventsRequest.getEnd(), "creationTime");
        Page<JobStatusTraceLog> page = jobStatusTraceLogRepository.findAll(specification, getPageable(findJobStatusTraceEventsRequest, JobStatusTraceLog.class));
        return new PageImpl<>(page.get().map(JobStatusTraceLog::toJobStatusTraceEvent).collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }
    
    private <T> Pageable getPageable(final BasePageRequest pageRequest, final Class<T> clazz) {
        int page = 0;
        int perPage = BasePageRequest.DEFAULT_PAGE_SIZE;
        if (pageRequest.getPageNumber() > 0 && pageRequest.getPageSize() > 0) {
            page = pageRequest.getPageNumber() - 1;
            perPage = pageRequest.getPageSize();
        }
        return PageRequest.of(page, perPage, getSort(pageRequest, clazz));
    }
    
    private <T> Sort getSort(final BasePageRequest pageRequest, final Class<T> clazz) {
        Sort sort = Sort.unsorted();
        boolean sortFieldIsPresent = Arrays.stream(clazz.getDeclaredFields())
            .map(Field::getName)
            .anyMatch(e -> e.equals(pageRequest.getSortBy()));
        if (!sortFieldIsPresent) {
            return sort;
        }
        if (!Strings.isNullOrEmpty(pageRequest.getSortBy())) {
            Sort.Direction order = Sort.Direction.ASC;
            try {
                order = Sort.Direction.valueOf(pageRequest.getOrderType());
            } catch (IllegalArgumentException ignored) {
            }
            sort = Sort.by(order, pageRequest.getSortBy());
        }
        return sort;
    }
    
    private <T> Specification<T> getSpecWithExampleAndDate(final Example<T> example, final Date from, final Date to, final String field) {
        return (Specification<T>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(builder.greaterThan(root.get(field), from));
            }
            if (to != null) {
                predicates.add(builder.lessThan(root.get(field), to));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private <T> Example<T> getExample(final Object source, final Class<T> clazz) {
        T instance = BeanUtils.newInstance(clazz);
        BeanUtils.copyProperties(source, instance);
        return Example.of(instance);
    }
}
