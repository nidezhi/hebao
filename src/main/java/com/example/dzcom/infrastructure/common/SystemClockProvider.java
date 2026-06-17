package com.example.dzcom.infrastructure.common;

import com.example.dzcom.application.common.service.ClockProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** 基于系统时钟提供项目统一业务时间。 */
@Component
public class SystemClockProvider implements ClockProvider {
    /** 项目数据库时间统一使用北京时间。 */
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前业务时间。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(BEIJING_ZONE);
    }
}
