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

package org.apache.shardingsphere.elasticjob.lite.console.dao.search;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.console.util.BeanUtils;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RDB job event search.
 */
@Slf4j
@Component
public final class RDBJobEventSearch {
    
    private final JobExecutionLogRepository jobExecutionLogRepository;
    
    private final JobStatusTraceLogRepository jobStatusTraceLogRepository;
    
    @Autowired
    public RDBJobEventSearch(final JobExecutionLogRepository jobExecutionLogRepository,
                             final JobStatusTraceLogRepository jobStatusTraceLogRepository) {
        this.jobExecutionLogRepository = jobExecutionLogRepository;
        this.jobStatusTraceLogRepository = jobStatusTraceLogRepository;
    }
    
    /**
     * Find job execution events.
     *
     * @param condition query condition
     * @return job execution events
     */
    public Result<JobExecutionEvent> findJobExecutionEvents(final Condition condition) {
        dealFields(condition.getFields());
        Page<JobExecutionEvent> jobExecutionEvents = getJobExecutionEvents(condition);
        return new Result<>(jobExecutionEvents.getTotalElements(), jobExecutionEvents.getContent());
    }
    
    private void dealFields(final Map<String, Object> fields) {
        if (Objects.isNull(fields)) {
            return;
        }
        Object isSuccessField = fields.get("isSuccess");
        if (!Objects.isNull(isSuccessField)) {
            fields.put("isSuccess", Objects.equals(isSuccessField, "1"));
        }
    }
    
    /**
     * Find job status trace events.
     *
     * @param condition query condition
     * @return job status trace events
     */
    public Result<JobStatusTraceEvent> findJobStatusTraceEvents(final Condition condition) {
        Page<JobStatusTraceEvent> jobStatusTraceEvents = getJobStatusTraceEvents(condition);
        return new Result<>(jobStatusTraceEvents.getTotalElements(), jobStatusTraceEvents.getContent());
    }
    
    private Page<JobExecutionEvent> getJobExecutionEvents(final Condition condition) {
        Specification<JobExecutionLog> specification =
                getSpecification(JobExecutionLog.class, condition, "startTime");
        Page<JobExecutionLog> page =
                jobExecutionLogRepository.findAll(specification, getPageable(condition, JobExecutionLog.class));
        return new PageImpl<>(
                page.get().map(JobExecutionLog::toJobExecutionEvent).collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements()
        );
    }
    
    private <T> Pageable getPageable(final Condition condition, final Class<T> clazz) {
        int page = 0;
        int perPage = Condition.DEFAULT_PAGE_SIZE;
        if (condition.getPage() > 0 && condition.getPerPage() > 0) {
            page = condition.getPage() - 1;
            perPage = condition.getPerPage();
        }
        return PageRequest.of(page, perPage, getSort(condition, clazz));
    }
    
    private <T> Sort getSort(final Condition condition, final Class<T> clazz) {
        Sort sort = Sort.unsorted();
        boolean sortFieldIsPresent = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .anyMatch(e -> e.equals(condition.getSort()));
        if (!sortFieldIsPresent) {
            return sort;
        }
        if (!Strings.isNullOrEmpty(condition.getSort())) {
            Sort.Direction order = Sort.Direction.ASC;
            try {
                order = Sort.Direction.valueOf(condition.getOrder());
            } catch (IllegalArgumentException ignored) {
            }
            sort = Sort.by(order, condition.getSort());
        }
        return sort;
    }
    
    private Page<JobStatusTraceEvent> getJobStatusTraceEvents(final Condition condition) {
        Specification<JobStatusTraceLog> specification =
                getSpecification(JobStatusTraceLog.class, condition, "creationTime");
        Page<JobStatusTraceLog> page =
                jobStatusTraceLogRepository.findAll(specification, getPageable(condition, JobStatusTraceLog.class));
        return new PageImpl<>(
                page.get().map(JobStatusTraceLog::toJobStatusTraceEvent).collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements()
        );
    }
    
    private <T> Specification<T> getSpecification(final Class<T> clazz, final Condition condition, final String dateField) {
        Example<T> example = getExample(condition.getFields(), clazz);
        return getSpecWithExampleAndDate(
                example, condition.getStartTime(), condition.getEndTime(), dateField
        );
    }
    
    private <T> Specification<T> getSpecWithExampleAndDate(
            final Example<T> example, final Date from, final Date to, final String field
    ) {
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
    
    private <T> Example<T> getExample(final Map<String, Object> fields, final Class<T> clazz) {
        T bean = BeanUtils.toBean(fields, clazz);
        if (Objects.isNull(bean)) {
            bean = BeanUtils.newInstance(clazz);
        }
        return Example.of(bean);
    }
    
    /**
     * Query condition.
     */
    @RequiredArgsConstructor
    @Getter
    public static class Condition {
        
        private static final int DEFAULT_PAGE_SIZE = 10;
        
        private final int perPage;
        
        private final int page;
        
        private final String sort;
        
        private final String order;
        
        private final Date startTime;
        
        private final Date endTime;
        
        private final Map<String, Object> fields;
    }
    
    @RequiredArgsConstructor
    @Getter
    public static class Result<T> {
        
        private final Long total;
        
        private final List<T> rows;
    }
}
