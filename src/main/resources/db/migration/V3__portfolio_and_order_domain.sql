-- ============================================================================
-- DZCOM 初始数据库 - V3 组合与订单域
-- 设计说明：
--   1. 投资组合主表不保存易漂移的资产汇总值，汇总值进入估值快照表。
--   2. 订单、成交和状态事件拆分，支持部分成交、重试、审计和渠道回执。
--   3. 所有用户、产品、组合、订单关联均为逻辑业务ID，不创建数据库外键。
--   4. 订单提供幂等键和乐观锁版本，避免重复下单与并发覆盖。
-- ============================================================================

-- 投资组合主表：保存组合稳定属性和生命周期状态。
CREATE TABLE aiw_portfolio (
    biz_id CHAR(36) NOT NULL COMMENT '投资组合业务ID，UUID字符串',
    portfolio_no VARCHAR(32) NOT NULL COMMENT '组合编号，面向用户和运营展示',
    owner_user_biz_id CHAR(36) NOT NULL COMMENT '组合所有者用户业务ID；逻辑关联账户域，无外键',
    portfolio_name VARCHAR(128) NOT NULL COMMENT '组合名称',
    portfolio_type VARCHAR(32) NOT NULL DEFAULT 'PERSONAL'
        COMMENT '组合类型：PERSONAL、MODEL、SIMULATION等',
    base_currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '组合基础计价币种',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '组合状态：0-关闭，1-正常，2-冻结',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    deleted_at DATETIME(3) NULL COMMENT '逻辑删除时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_portfolio_no (portfolio_no),
    KEY idx_aiw_portfolio_owner (owner_user_biz_id, status, is_deleted),
    KEY idx_aiw_portfolio_type_created (portfolio_type, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='投资组合主表；不保存实时资产汇总值';

-- 组合估值快照表：记录某一时点的组合资产、现金和盈亏。
CREATE TABLE aiw_portfolio_valuation (
    biz_id CHAR(36) NOT NULL COMMENT '估值快照业务ID，UUID字符串',
    portfolio_biz_id CHAR(36) NOT NULL COMMENT '组合业务ID；逻辑关联aiw_portfolio.biz_id，无外键',
    valuation_time DATETIME(3) NOT NULL COMMENT '估值时点',
    base_currency VARCHAR(8) NOT NULL COMMENT '估值计价币种',
    total_asset DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '组合总资产',
    cash_balance DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '现金余额',
    position_value DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '持仓市值',
    total_cost DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '持仓总成本',
    unrealized_profit DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '未实现盈亏',
    realized_profit DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '已实现盈亏',
    total_return_rate DECIMAL(16,10) NULL COMMENT '累计收益率',
    source_code VARCHAR(64) NOT NULL DEFAULT 'INTERNAL' COMMENT '估值来源编码',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_portfolio_valuation_time (portfolio_biz_id, valuation_time, source_code),
    KEY idx_aiw_portfolio_valuation_latest (portfolio_biz_id, valuation_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='投资组合估值快照表';

-- 当前持仓表：保存组合在产品维度上的聚合持仓。
CREATE TABLE aiw_position (
    biz_id CHAR(36) NOT NULL COMMENT '持仓业务ID，UUID字符串',
    portfolio_biz_id CHAR(36) NOT NULL COMMENT '组合业务ID；逻辑关联组合，无外键',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID；逻辑关联产品域，无外键',
    position_side VARCHAR(16) NOT NULL DEFAULT 'LONG' COMMENT '持仓方向：LONG、SHORT',
    quantity DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '当前持仓数量',
    available_quantity DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '当前可交易数量',
    average_cost DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '平均持仓成本',
    cost_amount DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '持仓成本金额',
    realized_profit DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '累计已实现盈亏',
    last_trade_at DATETIME(3) NULL COMMENT '最近一次影响该持仓的成交时间',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_position_dimension (portfolio_biz_id, product_biz_id, position_side),
    KEY idx_aiw_position_portfolio (portfolio_biz_id, is_deleted, updated_at),
    KEY idx_aiw_position_product (product_biz_id, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='投资组合当前持仓表';

-- 订单主表：保存用户投资指令及其当前状态。
CREATE TABLE aiw_order (
    biz_id CHAR(36) NOT NULL COMMENT '订单业务ID，UUID字符串',
    order_no VARCHAR(40) NOT NULL COMMENT '平台订单编号，面向用户和运营展示',
    idempotency_key VARCHAR(128) NULL COMMENT '客户端幂等键，同一用户范围内唯一',
    user_biz_id CHAR(36) NOT NULL COMMENT '下单用户业务ID；逻辑关联账户域，无外键',
    portfolio_biz_id CHAR(36) NOT NULL COMMENT '投资组合业务ID；逻辑关联组合，无外键',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID；逻辑关联产品域，无外键',
    channel_code VARCHAR(64) NOT NULL DEFAULT 'SIMULATOR' COMMENT '执行渠道编码',
    order_side VARCHAR(16) NOT NULL COMMENT '订单方向：BUY、SELL、SUBSCRIBE、REDEEM',
    order_type VARCHAR(16) NOT NULL COMMENT '订单类型：MARKET、LIMIT、AMOUNT等',
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '订单币种',
    requested_price DECIMAL(24,8) NULL COMMENT '委托价格；市价单可为空',
    requested_quantity DECIMAL(28,8) NULL COMMENT '委托数量；按金额下单时可为空',
    requested_amount DECIMAL(28,8) NULL COMMENT '委托金额；按数量下单时可为空',
    executed_quantity DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '累计成交数量',
    executed_amount DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '累计成交金额',
    fee_amount DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '累计费用',
    status VARCHAR(24) NOT NULL DEFAULT 'CREATED'
        COMMENT '订单状态：CREATED、SUBMITTED、PARTIAL_FILLED、FILLED、CANCELLED、REJECTED、FAILED',
    external_order_id VARCHAR(128) NULL COMMENT '执行渠道订单ID',
    reject_code VARCHAR(64) NULL COMMENT '拒绝或失败原因编码',
    reject_message VARCHAR(512) NULL COMMENT '脱敏后的拒绝或失败原因',
    submitted_at DATETIME(3) NULL COMMENT '提交执行渠道时间',
    completed_at DATETIME(3) NULL COMMENT '订单终态时间',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：订单原则上不物理删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_order_no (order_no),
    UNIQUE KEY uk_aiw_order_user_idempotency (user_biz_id, idempotency_key),
    KEY idx_aiw_order_user_status (user_biz_id, status, created_at),
    KEY idx_aiw_order_portfolio_status (portfolio_biz_id, status, created_at),
    KEY idx_aiw_order_product_time (product_biz_id, created_at),
    KEY idx_aiw_order_external (channel_code, external_order_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单主表；具备幂等、并发版本和渠道回执字段';

-- 成交明细表：一笔订单可产生多笔部分成交。
CREATE TABLE aiw_trade_execution (
    biz_id CHAR(36) NOT NULL COMMENT '成交业务ID，UUID字符串',
    execution_no VARCHAR(64) NOT NULL COMMENT '平台成交编号',
    order_biz_id CHAR(36) NOT NULL COMMENT '订单业务ID；逻辑关联订单，无外键',
    user_biz_id CHAR(36) NOT NULL COMMENT '用户业务ID，冗余保存用于查询和审计',
    portfolio_biz_id CHAR(36) NOT NULL COMMENT '组合业务ID，冗余保存用于查询',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID，冗余保存用于查询',
    channel_code VARCHAR(64) NOT NULL COMMENT '执行渠道编码',
    external_execution_id VARCHAR(128) NULL COMMENT '渠道成交ID',
    execution_price DECIMAL(24,8) NOT NULL COMMENT '成交价格',
    execution_quantity DECIMAL(28,8) NOT NULL COMMENT '成交数量',
    execution_amount DECIMAL(28,8) NOT NULL COMMENT '成交金额',
    fee_amount DECIMAL(28,8) NOT NULL DEFAULT 0 COMMENT '本笔成交费用',
    executed_at DATETIME(3) NOT NULL COMMENT '渠道成交时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_trade_execution_no (execution_no),
    UNIQUE KEY uk_aiw_trade_external (channel_code, external_execution_id),
    KEY idx_aiw_trade_order_time (order_biz_id, executed_at),
    KEY idx_aiw_trade_portfolio_time (portfolio_biz_id, executed_at),
    KEY idx_aiw_trade_product_time (product_biz_id, executed_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单成交明细表；支持部分成交';

-- 订单事件表：追加式记录状态变化和外部回执，禁止覆盖历史事件。
CREATE TABLE aiw_order_event (
    biz_id CHAR(36) NOT NULL COMMENT '订单事件业务ID，UUID字符串',
    order_biz_id CHAR(36) NOT NULL COMMENT '订单业务ID；逻辑关联订单，无外键',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型：CREATED、SUBMITTED、FILLED、CANCELLED等',
    from_status VARCHAR(24) NULL COMMENT '变更前状态',
    to_status VARCHAR(24) NULL COMMENT '变更后状态',
    event_source VARCHAR(32) NOT NULL DEFAULT 'INTERNAL' COMMENT '事件来源：INTERNAL、CHANNEL、OPERATOR',
    operator_biz_id VARCHAR(64) NULL COMMENT '操作者业务ID或系统标识',
    event_payload JSON NULL COMMENT '脱敏后的事件上下文和渠道回执摘要',
    occurred_at DATETIME(3) NOT NULL COMMENT '事件实际发生时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件入库时间',
    PRIMARY KEY (biz_id),
    KEY idx_aiw_order_event_order_time (order_biz_id, occurred_at),
    KEY idx_aiw_order_event_type_time (event_type, occurred_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='订单追加式事件表；用于状态追踪和审计';
