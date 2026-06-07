-- ============================================================
-- V4 风控、通知与审计领域
-- 设计约束：
-- 1. 禁止数据库外键，跨表仅保存逻辑业务标识。
-- 2. 风控规则与检查结果保留版本快照，避免规则变更影响历史结论。
-- 3. 审计日志只追加、不更新、不物理删除。
-- ============================================================

CREATE TABLE aiw_risk_rule (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '风险规则业务唯一标识',
    rule_code VARCHAR(64) NOT NULL COMMENT '规则编码，在同一版本内稳定',
    rule_version INT UNSIGNED NOT NULL COMMENT '规则版本号，从1递增',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(32) NOT NULL COMMENT '规则类型：ACCOUNT、ORDER、PORTFOLIO、COMPLIANCE等',
    risk_level VARCHAR(16) NOT NULL COMMENT '命中后的风险等级：LOW、MEDIUM、HIGH、CRITICAL',
    priority INT NOT NULL DEFAULT 100 COMMENT '执行优先级，数值越小越优先',
    rule_config JSON NOT NULL COMMENT '规则参数、阈值及条件配置',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、ENABLED、DISABLED、ARCHIVED',
    effective_at DATETIME(3) NULL COMMENT '生效时间，为空表示发布后立即生效',
    expired_at DATETIME(3) NULL COMMENT '失效时间，为空表示长期有效',
    created_by_biz_id CHAR(36) NULL COMMENT '创建人逻辑业务标识',
    updated_by_biz_id CHAR(36) NULL COMMENT '最后更新人逻辑业务标识',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_rule_biz_id (biz_id),
    UNIQUE KEY uk_risk_rule_code_version (rule_code, rule_version),
    KEY idx_risk_rule_type_status (rule_type, status),
    KEY idx_risk_rule_effective (status, effective_at, expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可版本化的风险控制规则';

CREATE TABLE aiw_risk_check (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '风险检查业务唯一标识',
    trace_id VARCHAR(64) NULL COMMENT '一次请求或业务链路的追踪标识',
    business_type VARCHAR(32) NOT NULL COMMENT '被检查业务类型：ACCOUNT、ORDER、PORTFOLIO等',
    business_biz_id CHAR(36) NOT NULL COMMENT '被检查对象的逻辑业务标识',
    user_biz_id CHAR(36) NULL COMMENT '关联用户逻辑业务标识，系统检查时可为空',
    rule_code VARCHAR(64) NOT NULL COMMENT '执行时采用的规则编码快照',
    rule_version INT UNSIGNED NOT NULL COMMENT '执行时采用的规则版本快照',
    check_result VARCHAR(16) NOT NULL COMMENT '检查结论：PASS、REVIEW、REJECT、ERROR',
    risk_level VARCHAR(16) NOT NULL COMMENT '本次检查判定的风险等级',
    score DECIMAL(10,4) NULL COMMENT '可选风险评分',
    reason_code VARCHAR(64) NULL COMMENT '机器可读的原因编码',
    detail JSON NULL COMMENT '命中条件、输入摘要及解释信息',
    checked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '执行检查时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_check_biz_id (biz_id),
    KEY idx_risk_check_business (business_type, business_biz_id, checked_at),
    KEY idx_risk_check_user (user_biz_id, checked_at),
    KEY idx_risk_check_rule (rule_code, rule_version, checked_at),
    KEY idx_risk_check_result (check_result, risk_level, checked_at),
    KEY idx_risk_check_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险检查结果与历史快照';

CREATE TABLE aiw_notification (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '通知业务唯一标识',
    recipient_type VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT '接收方类型：USER、ROLE、SYSTEM',
    recipient_biz_id CHAR(36) NOT NULL COMMENT '接收方逻辑业务标识',
    channel VARCHAR(16) NOT NULL COMMENT '通知渠道：IN_APP、EMAIL、SMS、PUSH',
    notification_type VARCHAR(32) NOT NULL COMMENT '通知类型：ACCOUNT、ORDER、RISK、SYSTEM等',
    template_code VARCHAR(64) NULL COMMENT '消息模板编码，为空表示直接内容',
    title VARCHAR(256) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知正文或已渲染内容',
    payload JSON NULL COMMENT '跳转参数、模板变量及扩展数据',
    business_type VARCHAR(32) NULL COMMENT '来源业务类型',
    business_biz_id CHAR(36) NULL COMMENT '来源业务逻辑标识',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态：PENDING、SENDING、SENT、FAILED、CANCELLED',
    read_status VARCHAR(16) NOT NULL DEFAULT 'UNREAD' COMMENT '阅读状态：UNREAD、READ',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发送重试次数',
    next_retry_at DATETIME(3) NULL COMMENT '下一次允许重试时间',
    sent_at DATETIME(3) NULL COMMENT '成功发送时间',
    read_at DATETIME(3) NULL COMMENT '用户阅读时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_biz_id (biz_id),
    KEY idx_notification_recipient (recipient_type, recipient_biz_id, created_at),
    KEY idx_notification_unread (recipient_biz_id, read_status, created_at),
    KEY idx_notification_dispatch (status, next_retry_at, created_at),
    KEY idx_notification_business (business_type, business_biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多渠道通知及站内消息';

CREATE TABLE aiw_audit_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '审计日志业务唯一标识',
    trace_id VARCHAR(64) NULL COMMENT '请求链路追踪标识',
    operator_type VARCHAR(16) NOT NULL COMMENT '操作者类型：USER、ADMIN、SYSTEM、JOB',
    operator_biz_id CHAR(36) NULL COMMENT '操作者逻辑业务标识，系统操作时可为空',
    action_code VARCHAR(64) NOT NULL COMMENT '稳定的操作编码，例如 ACCOUNT_DISABLE',
    resource_type VARCHAR(32) NOT NULL COMMENT '被操作资源类型',
    resource_biz_id CHAR(36) NULL COMMENT '被操作资源逻辑业务标识',
    request_method VARCHAR(16) NULL COMMENT 'HTTP请求方法或任务类型',
    request_path VARCHAR(512) NULL COMMENT '请求路径或任务名称',
    result_code VARCHAR(64) NOT NULL COMMENT '业务结果码',
    result_status VARCHAR(16) NOT NULL COMMENT '结果状态：SUCCESS、FAILURE',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP，兼容IPv4和IPv6',
    user_agent VARCHAR(512) NULL COMMENT '客户端User-Agent摘要',
    detail JSON NULL COMMENT '脱敏后的变更摘要、失败原因及上下文',
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作实际发生时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '日志入库时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_audit_log_biz_id (biz_id),
    KEY idx_audit_log_operator (operator_type, operator_biz_id, occurred_at),
    KEY idx_audit_log_resource (resource_type, resource_biz_id, occurred_at),
    KEY idx_audit_log_action (action_code, occurred_at),
    KEY idx_audit_log_trace (trace_id),
    KEY idx_audit_log_occurred (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='只追加的关键业务操作审计日志';
