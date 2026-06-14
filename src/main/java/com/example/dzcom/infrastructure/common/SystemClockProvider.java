package com.example.dzcom.infrastructure.common;

import com.example.dzcom.application.common.service.ClockProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class SystemClockProvider implements ClockProvider {
    /**
     * 获取当前业务时间。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
