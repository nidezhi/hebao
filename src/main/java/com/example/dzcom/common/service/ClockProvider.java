package com.example.dzcom.common.service;

import java.time.LocalDateTime;

public interface ClockProvider {
    /**
     * 获取当前业务时间。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    LocalDateTime now();
}
