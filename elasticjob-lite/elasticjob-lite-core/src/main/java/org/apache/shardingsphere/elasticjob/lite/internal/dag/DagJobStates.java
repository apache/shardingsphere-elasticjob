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

public enum DagJobStates {
    NONE("none"),
    REG("register"),
    INIT("graph"),
    READY("ready"),
    RUNNING("running"),
    PAUSE("pause"),
    FAIL("fail"),
    SUCCESS("success"),
    SKIP("skip"),
    RETRY("retry"),
    ERROR("error");

    private String value;
    DagJobStates(final String value) {
        this.value = value;
    }

    /**
     * Give string return enums.
     *
     * @param value enum string value
     * @return DagJobStates enum
     */
    public static DagJobStates of(final String value) {
        for (DagJobStates states : DagJobStates.values()) {
            if (StringUtils.equalsIgnoreCase(value, states.getValue())) {
                return states;
            }
        }
        return DagJobStates.NONE;
    }

    /**
     * Get value.
     *
     * @return string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value enum value
     */
    public void setValue(final String value) {
        this.value = value;
    }

}
