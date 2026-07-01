ALTER TABLE aiw_order
    MODIFY COLUMN idempotency_key VARCHAR(512) NULL COMMENT '客户端幂等键，同一用户范围内唯一，保留自动闭环可读追踪上下文';
