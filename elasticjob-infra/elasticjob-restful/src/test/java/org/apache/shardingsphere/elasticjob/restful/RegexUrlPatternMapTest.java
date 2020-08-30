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

import org.apache.shardingsphere.elasticjob.restful.mapping.MappingContext;
import org.apache.shardingsphere.elasticjob.restful.mapping.RegexUrlPatternMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class RegexUrlPatternMapTest {
    
    @Test
    public void assertRegexUrlPatternMap() {
        RegexUrlPatternMap<Integer> urlPatternMap = new RegexUrlPatternMap<>();
        urlPatternMap.put("/app/{jobName}", 1);
        urlPatternMap.put("/app/list", 2);
        urlPatternMap.put("/app/{jobName}/disable", 3);
        urlPatternMap.put("/app/{jobName}/enable", 4);
        MappingContext<Integer> mappingContext = urlPatternMap.match("/app/myJob");
        assertNotNull(mappingContext);
        assertThat(mappingContext.pattern(), is("/app/{jobName}"));
        assertThat(mappingContext.payload(), is(1));
        mappingContext = urlPatternMap.match("/app/list");
        assertNotNull(mappingContext);
        assertThat(mappingContext.pattern(), is("/app/list"));
        assertThat(mappingContext.payload(), is(2));
        mappingContext = urlPatternMap.match("/job/list");
        assertNull(mappingContext);
    }
    
    @Test
    public void assertAmbiguous() {
        RegexUrlPatternMap<Integer> urlPatternMap = new RegexUrlPatternMap<>();
        urlPatternMap.put("/foo/{bar}/{fooName}/status", 10);
        urlPatternMap.put("/foo/{bar}/operate/{metrics}", 11);
        MappingContext<Integer> mappingContext = urlPatternMap.match("/foo/barValue/operate/status");
        assertNotNull(mappingContext);
        assertThat(mappingContext.pattern(), is("/foo/{bar}/operate/{metrics}"));
        assertThat(mappingContext.payload(), is(11));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertDuplicate() {
        RegexUrlPatternMap<Integer> urlPatternMap = new RegexUrlPatternMap<>();
        urlPatternMap.put("/app/{jobName}/enable", 0);
        urlPatternMap.put("/app/{jobName}", 1);
        urlPatternMap.put("/app/{appName}", 2);
    }
}
