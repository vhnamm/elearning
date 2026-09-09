package com.hnv.elearning.infrastructure.redis;

import java.time.Duration;

public interface RedisService {
    void set(String key, Object value);

    void set(String key, Object value, Duration ttl);

    boolean exists(String key);

    void delete(String key);

    void setIfAbsent(String key, String value);

    <T> T get(String key, Class<T> clazz);
}
