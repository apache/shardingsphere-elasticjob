/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.internal.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SensitiveInfoUtilsTest {
    
    @Test
    public void assertFilterContentWithoutIp() {
        List<String> actual = Arrays.asList("/simpleElasticDemoJob/servers", "/simpleElasticDemoJob/leader");
        assertThat(SensitiveInfoUtils.filterSensitiveIps(actual), is(actual));
    }
    
    @Test
    public void assertFilterContentWithSensitiveIp() {
        List<String> actual = Arrays.asList("/simpleElasticDemoJob/servers/127.0.0.1", "/simpleElasticDemoJob/servers/192.168.0.1/hostName | 192.168.0.1");
        List<String> expected = Arrays.asList("/simpleElasticDemoJob/servers/ip1", "/simpleElasticDemoJob/servers/ip2/hostName | ip2");
        assertThat(SensitiveInfoUtils.filterSensitiveIps(actual), is(expected));
    }
}
