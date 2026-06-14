package com.example.dzcom.infrastructure.utils;

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
     *
     * @param key 数据键
     * @param value 待处理的数据值
     * @param timeout timeout 参数
     * @param unit unit 参数
     * @author dz
     * @date 2026-06-14
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 获取字符串
     *
     * @param key 数据键
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除键
     *
     * @param key 数据键
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 判断键是否存在
     *
     * @param key 数据键
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置Hash
     *
     * @param key 数据键
     * @param field field 参数
     * @param value 待处理的数据值
     * @author dz
     * @date 2026-06-14
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }
    
    /**
     * 获取Hash
     *
     * @param key 数据键
     * @param field field 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }
    
    /**
     * 设置过期时间
     *
     * @param key 数据键
     * @param timeout timeout 参数
     * @param unit unit 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}
