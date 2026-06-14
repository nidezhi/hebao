package com.example.dzcom.infrastructure.common;

import com.example.dzcom.common.service.IdGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Component
public class UuidIdGenerator implements IdGenerator {
    private final SecureRandom random = new SecureRandom();

    /**
     * 执行 new biz id 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public String newBizId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 执行 new user no 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public String newUserNo() {
        return "U" + Instant.now().toEpochMilli() + String.format("%04d", random.nextInt(10_000));
    }
}
