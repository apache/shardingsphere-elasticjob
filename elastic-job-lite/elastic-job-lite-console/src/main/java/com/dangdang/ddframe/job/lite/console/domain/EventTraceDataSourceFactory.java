package com.dangdang.ddframe.job.lite.console.domain;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件追踪数据源工厂.
 *
 * @author zhangxinguo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTraceDataSourceFactory {
    
    private static final ConcurrentHashMap<HashCode, EventTraceDataSource> DATA_SOURCE_REGISTRY = new ConcurrentHashMap<>(); 
    
    /**
     * 创建事件追踪数据源.
     *
     * @return 事件追踪数据源对象
     */
    public static EventTraceDataSource createEventTraceDataSource(final String driver, final String url, final String username, final Optional<String> password) {
        Hasher hasher =  Hashing.md5().newHasher().putString(driver, Charsets.UTF_8).putString(url, Charsets.UTF_8);
        if (!Strings.isNullOrEmpty(username)) {
            hasher.putString(username, Charsets.UTF_8);
        }
        if (password.isPresent()) {
            hasher.putString(password.get(), Charsets.UTF_8);
        }
        HashCode hashCode = hasher.hash();
        EventTraceDataSource result = DATA_SOURCE_REGISTRY.get(hashCode);
        if (null != result) {
            return result;
        }
        EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration = new EventTraceDataSourceConfiguration(driver, url, username);
        if (password.isPresent()) {
            eventTraceDataSourceConfiguration.setPassword(password.get());
        }
        result = new EventTraceDataSource(eventTraceDataSourceConfiguration);
        result.init();
        DATA_SOURCE_REGISTRY.put(hashCode, result);
        return result;
    }
}
