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

package org.apache.shardingsphere.elasticjob.lite.spring.core.scanner;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to register Elastic job.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Import(ElasticJobScanRegistrar.class)
@Target(ElementType.TYPE)
public @interface ElasticJobScan {
    
    /**
     * Alias for the {@link #basePackages()} attribute.
     *
     * @return Base packages name
     */
    String[] value() default "";

    /**
     * Base packages to scan for Elastic job.
     *
     * @return Base packages name
     */
    String[] basePackages() default {};
}
