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

package org.apache.shardingsphere.elasticjob.lite.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sensitive info utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveInfoUtils {
    
    private static final String FAKE_IP_SAMPLE = "ip";

    private static final Pattern IP_PATTERN = Pattern.compile(IpUtils.IP_REGEX);
    
    /**
     * Filter sensitive IP addresses.
     * 
     * @param target IP addresses to be filtered
     * @return filtered IP addresses
     */
    public static List<String> filterSensitiveIps(final List<String> target) {
        final Map<String, String> fakeIpMap = new HashMap<>();
        final AtomicInteger step = new AtomicInteger();
        return target.stream().map(input -> {
            Matcher matcher = IP_PATTERN.matcher(input);
            String result = input;
            while (matcher.find()) {
                String realIp = matcher.group();
                String fakeIp;
                if (fakeIpMap.containsKey(realIp)) {
                    fakeIp = fakeIpMap.get(realIp);
                } else {
                    fakeIp = FAKE_IP_SAMPLE + step.incrementAndGet();
                    fakeIpMap.put(realIp, fakeIp);
                }
                result = result.replace(realIp, fakeIp);
            }
            return result;
        }).collect(Collectors.toList());
    }
}
