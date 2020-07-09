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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Extraction type.
 */
@NoArgsConstructor
public final class SupportedExtractionType {
    
    private static final Set<String> EXTRACTION_TYPES = new HashSet<>(9, 1);
    
    static {
        EXTRACTION_TYPES.add(".tar");
        EXTRACTION_TYPES.add(".tar.gz");
        EXTRACTION_TYPES.add(".tar.bz2");
        EXTRACTION_TYPES.add(".tar.xz");
        EXTRACTION_TYPES.add(".gz");
        EXTRACTION_TYPES.add(".tgz");
        EXTRACTION_TYPES.add(".tbz2");
        EXTRACTION_TYPES.add(".txz");
        EXTRACTION_TYPES.add(".zip");
    }
    
    /**
     * Check whether the url is supported to extract or not.
     *
     * @param appURL app url
     * @return true is the url supported, otherwise not
     */
    public static boolean isExtraction(final String appURL) {
        for (String each : EXTRACTION_TYPES) {
            if (appURL.endsWith(each)) {
                return true;
            }
        }
        return false;
    }
}
