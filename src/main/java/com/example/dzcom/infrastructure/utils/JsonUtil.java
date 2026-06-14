package com.example.dzcom.infrastructure.utils;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * JSON工具类
 */
public class JsonUtil {
    
    /**
     * 对象转JSON字符串
     *
     * @param obj obj 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }
    
    /**
     * JSON字符串转对象
     *
     * @param json json 参数
     * @param clazz 目标数据类型
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    /**
     * JSON字符串转List
     *
     * @param json json 参数
     * @param clazz 目标数据类型
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }
    
    /**
     * 创建并初始化 JsonUtil 对象。
     *
     * @author dz
     * @date 2026-06-14
     */
    private JsonUtil() {
        // 防止实例化
    }
}
