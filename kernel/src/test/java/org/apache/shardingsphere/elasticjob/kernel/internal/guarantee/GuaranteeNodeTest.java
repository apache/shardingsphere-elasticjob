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

package org.apache.shardingsphere.elasticjob.kernel.internal.guarantee;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuaranteeNodeTest {
    
    private final GuaranteeNode guaranteeNode = new GuaranteeNode("test_job");
    
    @Test
    void assertGetStartedNode() {
        assertThat(GuaranteeNode.getStartedNode(1), is("guarantee/started/1"));
    }
    
    @Test
    void assertGetCompletedNode() {
        assertThat(GuaranteeNode.getCompletedNode(1), is("guarantee/completed/1"));
    }
    
    @Test
    void assertIsStartedRootNode() {
        assertTrue(guaranteeNode.isStartedRootNode("/test_job/guarantee/started"));
    }
    
    @Test
    void assertIsNotStartedRootNode() {
        assertFalse(guaranteeNode.isStartedRootNode("/otherJob/guarantee/started"));
    }
    
    @Test
    void assertIsCompletedRootNode() {
        assertTrue(guaranteeNode.isCompletedRootNode("/test_job/guarantee/completed"));
    }
    
    @Test
    void assertIsNotCompletedRootNode() {
        assertFalse(guaranteeNode.isCompletedRootNode("/otherJob/guarantee/completed"));
    }
}
