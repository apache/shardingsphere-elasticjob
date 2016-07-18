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

package com.dangdang.ddframe.job.cloud.internal.reg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 敏感信息过滤工具类.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveInfoUtils {
    
    public static List<String> filterSensitiveIps(final List<String> result) {
        final Map<String, String> fakeIpMap = new HashMap<>();
        final String fakeIpSample = "ip";
        final AtomicInteger step = new AtomicInteger();
        Function<String, String> func = new Function<String, String>() {
            
            @Override
            public String apply(final String line) {
                final String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
                final Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                String result = line;
                while (matcher.find()) {
                    String realIp = matcher.group();
                    String fakeIp = fakeIpMap.get(realIp);
                    if (Strings.isNullOrEmpty(fakeIp)) {
                        fakeIp = fakeIpSample + step.incrementAndGet();
                        fakeIpMap.put(realIp, fakeIp);
                    }
                    result = result.replace(realIp, fakeIp);
                }
                return result;
            }
        };
        return Lists.transform(result, func);
    }
}
