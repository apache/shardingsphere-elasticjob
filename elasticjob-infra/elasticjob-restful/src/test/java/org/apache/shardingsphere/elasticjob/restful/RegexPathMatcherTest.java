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

package org.apache.shardingsphere.elasticjob.restful;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RegexPathMatcherTest {
    
    @Test
    public void testCaptureTemplate() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        Map<String, String> variables = pathMatcher.captureVariables("/app/{jobName}/disable/{until}/done", "/app/myJob/disable/20201231/done?name=some_name&value=some_value");
        assertFalse(variables.isEmpty());
        assertEquals(2, variables.size());
        assertEquals("myJob", variables.get("jobName"));
        assertEquals("20201231", variables.get("until"));
        assertNull(variables.get("app"));
    }
    
    @Test
    public void testCapturePatternWithoutTemplate() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        Map<String, String> variables = pathMatcher.captureVariables("/app", "/app");
        assertTrue(variables.isEmpty());
    }
    
    @Test
    public void testMatch() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        assertTrue(pathMatcher.matches("/app/{jobName}", "/app/myJob"));
    }
    
    @Test
    public void testValidatePathPattern() {
    
    }
}
