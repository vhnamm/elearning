package com.hnv.elearning.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisServiceImp implements RedisService {
    private final RedisTemplate redisTemplate;

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void setIfAbsent(String key, String value) {
        redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForValue().get(key);
    }
}
