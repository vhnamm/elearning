package com.hnv.elearning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory());

//        Trong Java: Một Object (ví dụ: UserRequest, LoginResponse, HashMap) thực chất là một vùng nhớ RAM phức tạp chứa các địa chỉ con (memory addresses/pointers) chỉ đến các ô nhớ khác.
//
//        Trong Redis: Redis là một Database độc lập (chạy ở một Process khác, thậm chí trên một máy chủ khác). Redis không hề hiểu Object Java là cái gì, nó chỉ hiểu các chuỗi Byte (byte[]), Text (String), hay JSON.
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}


