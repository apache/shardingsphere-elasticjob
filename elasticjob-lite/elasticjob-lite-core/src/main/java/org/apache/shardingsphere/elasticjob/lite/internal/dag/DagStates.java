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

package org.apache.shardingsphere.elasticjob.lite.internal.dag;

import org.apache.commons.lang3.StringUtils;

/**
 * Dag state.
 *
 */
public enum DagStates {
    NONE("none"),

    RUNNING("running"),

    PAUSE("pause"),

    FAIL("fail"),

    SUCCESS("success");

    private String value;

    DagStates(final String value) {
        this.value = value;
    }

    /**
     * give value return Enum.
     *
     * @param value enum value
     * @return DagStates enum.
     */
    public static DagStates of(final String value) {
        for (DagStates states : DagStates.values()) {
            if (StringUtils.equalsIgnoreCase(value, states.getValue())) {
                return states;
            }
        }
        return DagStates.NONE;
    }

    /**
     * Get enum value.
     *
     * @return string
     */
    public String getValue() {
        return value;
    }
}
