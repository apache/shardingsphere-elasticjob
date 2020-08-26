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

import org.apache.shardingsphere.elasticjob.restful.mapping.PathMatcher;
import org.apache.shardingsphere.elasticjob.restful.mapping.RegexPathMatcher;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RegexPathMatcherTest {
    
    @Test
    public void assertCaptureTemplate() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        Map<String, String> variables = pathMatcher.captureVariables("/app/{jobName}/disable/{until}/done", "/app/myJob/disable/20201231/done?name=some_name&value=some_value");
        assertFalse(variables.isEmpty());
        assertThat(variables.size(), is(2));
        assertThat(variables.get("jobName"), is("myJob"));
        assertThat(variables.get("until"), is("20201231"));
        assertNull(variables.get("app"));
    }
    
    @Test
    public void assertCapturePatternWithoutTemplate() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        Map<String, String> variables = pathMatcher.captureVariables("/app", "/app");
        assertTrue(variables.isEmpty());
    }
    
    @Test
    public void assertPathMatch() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        assertTrue(pathMatcher.matches("/app/{jobName}", "/app/myJob"));
    }
    
    @Test
    public void assertValidatePathPattern() {
        PathMatcher pathMatcher = new RegexPathMatcher();
        assertTrue(pathMatcher.isValidPathPattern("/"));
        assertTrue(pathMatcher.isValidPathPattern("/app"));
        assertTrue(pathMatcher.isValidPathPattern("/app/job"));
        assertTrue(pathMatcher.isValidPathPattern("/app/job/"));
        assertTrue(pathMatcher.isValidPathPattern("/app/{jobName}"));
        assertTrue(pathMatcher.isValidPathPattern("/{appName}/{jobName}/status"));
        assertFalse(pathMatcher.isValidPathPattern("/app/jobName}"));
        assertFalse(pathMatcher.isValidPathPattern("/app/{jobName"));
        assertFalse(pathMatcher.isValidPathPattern("/app/{job}Name"));
        assertFalse(pathMatcher.isValidPathPattern("/app//jobName"));
        assertFalse(pathMatcher.isValidPathPattern("//app/jobName"));
        assertFalse(pathMatcher.isValidPathPattern("app/jobName"));
        assertFalse(pathMatcher.isValidPathPattern(""));
    }
}
