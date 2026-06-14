package com.example.dzcom.application.common.service;

public interface IdGenerator {
    /**
     * 执行 new biz id 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    String newBizId();

    /**
     * 执行 new user no 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    String newUserNo();
}
