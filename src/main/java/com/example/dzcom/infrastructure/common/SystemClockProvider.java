package com.example.dzcom.infrastructure.common;

import com.example.dzcom.common.service.ClockProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class SystemClockProvider implements ClockProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
