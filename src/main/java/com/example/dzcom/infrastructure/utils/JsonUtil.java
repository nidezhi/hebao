package com.example.dzcom.infrastructure.utils;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * JSON工具类
 */
public class JsonUtil {
    
    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }
    
    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    /**
     * JSON字符串转List
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }
    
    private JsonUtil() {
        // 防止实例化
    }
}
