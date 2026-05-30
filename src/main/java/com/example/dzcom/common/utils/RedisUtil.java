package com.example.dzcom.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 设置字符串
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 获取字符串
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置Hash
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }
    
    /**
     * 获取Hash
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }
    
    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}
