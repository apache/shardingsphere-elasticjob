/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.guarantee;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GuaranteeNodeTest {
    
    private GuaranteeNode guaranteeNode = new GuaranteeNode("testJob");
    
    @Test
    public void testGetStartedNode() {
        assertThat(GuaranteeNode.getStartedNode(1), is("guarantee/started/1"));
    }
    
    @Test
    public void testGetCompletedNode() {
        assertThat(GuaranteeNode.getCompletedNode(1), is("guarantee/completed/1"));
    }
    
    @Test
    public void testIsStartedRootNode() {
        assertTrue(guaranteeNode.isStartedRootNode("/testJob/guarantee/started"));
    }
    
    @Test
    public void testIsNotStartedRootNode() {
        assertFalse(guaranteeNode.isStartedRootNode("/otherJob/guarantee/started"));
    }
    
    @Test
    public void testIsCompletedRootNode() {
        assertTrue(guaranteeNode.isCompletedRootNode("/testJob/guarantee/completed"));
    }
    
    @Test
    public void testIsNotCompletedRootNode() {
        assertFalse(guaranteeNode.isCompletedRootNode("/otherJob/guarantee/completed"));
    }
}
