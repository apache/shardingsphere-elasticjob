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

package com.dangdang.ddframe.job.lite.internal.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息过滤工具类.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveInfoUtils {
    
    private static final String FAKE_IP_SAMPLE = "ip";
    
    private static final String IP_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    
    /**
     * 屏蔽替换IP地址敏感信息.
     * 
     * @param target 待替换敏感信息的字符串列表
     * @return 替换敏感信息后的字符串列表
     */
    public static List<String> filterSensitiveIps(final List<String> target) {
        final Map<String, String> fakeIpMap = new HashMap<>();
        final AtomicInteger step = new AtomicInteger();
        return Lists.transform(target, new Function<String, String>() {
            
            @Override
            public String apply(final String input) {
                Matcher matcher = Pattern.compile(IP_REGEX).matcher(input);
                String result = input;
                while (matcher.find()) {
                    String realIp = matcher.group();
                    String fakeIp;
                    if (fakeIpMap.containsKey(realIp)) {
                        fakeIp = fakeIpMap.get(realIp);
                    } else {
                        fakeIp = Joiner.on("").join(FAKE_IP_SAMPLE, step.incrementAndGet());
                        fakeIpMap.put(realIp, fakeIp);
                    }
                    result = result.replace(realIp, fakeIp);
                }
                return result;
            }
        });
    }
}
