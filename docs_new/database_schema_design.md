# AI 理财平台 - 数据库表结构设计

本文档定义了AI理财平台的核心数据表结构，采用统一的主GUID（`biz_id`）关联所有业务表的设计理念，确保业务数据的完整性和可追溯性。

## 设计原则

1. **统一业务ID**: 每个核心业务实体使用 `biz_id` (UUID) 作为主键，所有关联表通过 `biz_id` 进行关联
2. **分层设计**: 按业务域划分表结构，保持模块独立性
3. **审计字段**: 所有表包含创建时间、更新时间、操作人等审计字段
4. **软删除**: 使用 `is_deleted` 标记实现软删除
5. **简化设计**: MVP阶段保持表结构简洁，后续可扩展

---

## 1. 用户域 (User Domain)

### 1.1 用户基础表 (aiw_user)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 用户业务ID (UUID) | PRIMARY KEY |
| user_no | VARCHAR(32) | 用户编号 (业务可读) | UNIQUE, NOT NULL |
| username | VARCHAR(64) | 用户名 | NOT NULL |
| email | VARCHAR(128) | 邮箱 | UNIQUE |
| phone | VARCHAR(20) | 手机号 | UNIQUE |
| password_hash | VARCHAR(256) | 密码哈希 | NOT NULL |
| kyc_status | TINYINT | KYC状态 (0:未认证, 1:已认证, 2:审核中) | DEFAULT 0 |
| risk_level | TINYINT | 风险承受能力等级 (1-5) | DEFAULT 1 |
| status | TINYINT | 账户状态 (0:禁用, 1:正常) | DEFAULT 1 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| created_by | VARCHAR(64) | 创建人 | |
| updated_by | VARCHAR(64) | 更新人 | |
| is_deleted | TINYINT | 是否删除 (0:否, 1:是) | DEFAULT 0 |

**索引**: 
- idx_user_no (user_no)
- idx_email (email)
- idx_phone (phone)

### 1.2 用户偏好表 (aiw_user_preference)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY, AUTO_INCREMENT |
| biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| preference_key | VARCHAR(64) | 偏好键 | NOT NULL |
| preference_value | TEXT | 偏好值 | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: biz_id -> aiw_user.biz_id

---

## 2. 产品域 (Product Domain)

### 2.1 产品基础表 (aiw_product)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 产品业务ID (UUID) | PRIMARY KEY |
| product_code | VARCHAR(32) | 产品代码 (如股票代码) | UNIQUE, NOT NULL |
| product_name | VARCHAR(128) | 产品名称 | NOT NULL |
| product_type | VARCHAR(32) | 产品类型 (stock/fund/bond/etf/option/future) | NOT NULL |
| exchange | VARCHAR(32) | 交易所 | |
| currency | VARCHAR(8) | 币种 (CNY/USD/HKD) | DEFAULT 'CNY' |
| min_invest_amount | DECIMAL(18,2) | 最小投资金额 | DEFAULT 0 |
| fee_rate | DECIMAL(6,4) | 费率 | DEFAULT 0 |
| status | TINYINT | 产品状态 (0:下架, 1:上架) | DEFAULT 1 |
| listing_date | DATE | 上市日期 | |
| description | TEXT | 产品描述 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**索引**:
- idx_product_code (product_code)
- idx_product_type (product_type)
- idx_exchange (exchange)

### 2.2 产品元数据表 (aiw_product_metadata)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY, AUTO_INCREMENT |
| biz_id | VARCHAR(64) | 产品业务ID | NOT NULL, INDEX |
| metadata_key | VARCHAR(64) | 元数据键 (如pe_ratio, market_cap) | NOT NULL |
| metadata_value | VARCHAR(512) | 元数据值 | NOT NULL |
| data_date | DATE | 数据日期 | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: biz_id -> aiw_product.biz_id  
**索引**: idx_biz_key_date (biz_id, metadata_key, data_date)

---

## 3. 市场数据域 (Market Data Domain)

### 3.1 行情数据表 (aiw_market_quote)

> 注: 实际生产环境建议使用时序数据库(ClickHouse/TimescaleDB)，此处为关系型数据库简化设计

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY, AUTO_INCREMENT |
| product_biz_id | VARCHAR(64) | 产品业务ID | NOT NULL, INDEX |
| quote_time | DATETIME | 行情时间 | NOT NULL |
| open_price | DECIMAL(18,4) | 开盘价 | |
| high_price | DECIMAL(18,4) | 最高价 | |
| low_price | DECIMAL(18,4) | 最低价 | |
| close_price | DECIMAL(18,4) | 收盘价 | |
| volume | BIGINT | 成交量 | |
| amount | DECIMAL(18,2) | 成交额 | |
| created_at | DATETIME | 创建时间 | NOT NULL |

**索引**: 
- idx_product_time (product_biz_id, quote_time)
- idx_quote_time (quote_time)

### 3.2 新闻公告表 (aiw_news_article)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 新闻业务ID | PRIMARY KEY |
| title | VARCHAR(256) | 标题 | NOT NULL |
| content | LONGTEXT | 内容 | |
| source | VARCHAR(128) | 来源 | |
| publish_time | DATETIME | 发布时间 | NOT NULL |
| related_products | JSON | 相关产品ID列表 | |
| sentiment_score | DECIMAL(3,2) | 情绪评分 (-1到1) | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**索引**: idx_publish_time (publish_time)

---

## 4. 组合与投资域 (Portfolio & Orders Domain)

### 4.1 投资组合表 (aiw_portfolio)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 组合业务ID (UUID) | PRIMARY KEY |
| user_biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| portfolio_name | VARCHAR(128) | 组合名称 | NOT NULL |
| total_asset | DECIMAL(18,2) | 总资产 | DEFAULT 0 |
| available_cash | DECIMAL(18,2) | 可用现金 | DEFAULT 0 |
| total_profit | DECIMAL(18,2) | 总盈亏 | DEFAULT 0 |
| profit_rate | DECIMAL(6,4) | 收益率 | DEFAULT 0 |
| status | TINYINT | 状态 (0:关闭, 1:活跃) | DEFAULT 1 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: user_biz_id -> aiw_user.biz_id  
**索引**: idx_user_biz_id (user_biz_id)

### 4.2 持仓明细表 (aiw_position)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 持仓业务ID (UUID) | PRIMARY KEY |
| portfolio_biz_id | VARCHAR(64) | 组合业务ID | NOT NULL, INDEX |
| product_biz_id | VARCHAR(64) | 产品业务ID | NOT NULL, INDEX |
| quantity | DECIMAL(18,4) | 持有数量 | NOT NULL |
| avg_cost | DECIMAL(18,4) | 平均成本 | NOT NULL |
| current_price | DECIMAL(18,4) | 当前价格 | |
| market_value | DECIMAL(18,2) | 市值 | |
| unrealized_profit | DECIMAL(18,2) | 未实现盈亏 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: 
- portfolio_biz_id -> aiw_portfolio.biz_id
- product_biz_id -> aiw_product.biz_id

**索引**: 
- idx_portfolio_product (portfolio_biz_id, product_biz_id)

### 4.3 订单表 (aiw_order)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 订单业务ID (UUID) | PRIMARY KEY |
| order_no | VARCHAR(32) | 订单编号 (业务可读) | UNIQUE, NOT NULL |
| user_biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| portfolio_biz_id | VARCHAR(64) | 组合业务ID | NOT NULL, INDEX |
| product_biz_id | VARCHAR(64) | 产品业务ID | NOT NULL, INDEX |
| order_type | VARCHAR(16) | 订单类型 (BUY/SELL) | NOT NULL |
| order_price | DECIMAL(18,4) | 订单价格 | NOT NULL |
| quantity | DECIMAL(18,4) | 数量 | NOT NULL |
| total_amount | DECIMAL(18,2) | 总金额 | NOT NULL |
| status | VARCHAR(16) | 订单状态 (PENDING/FILLED/CANCELLED/FAILED) | DEFAULT 'PENDING' |
| executed_price | DECIMAL(18,4) | 成交价格 | |
| executed_quantity | DECIMAL(18,4) | 成交数量 | |
| executed_at | DATETIME | 成交时间 | |
| broker_order_id | VARCHAR(64) | 券商订单ID | |
| remark | VARCHAR(512) | 备注 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: 
- user_biz_id -> aiw_user.biz_id
- portfolio_biz_id -> aiw_portfolio.biz_id
- product_biz_id -> aiw_product.biz_id

**索引**: 
- idx_order_no (order_no)
- idx_user_status (user_biz_id, status)
- idx_created_at (created_at)

### 4.4 订单流水表 (aiw_order_log)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY, AUTO_INCREMENT |
| order_biz_id | VARCHAR(64) | 订单业务ID | NOT NULL, INDEX |
| log_type | VARCHAR(32) | 日志类型 (CREATE/UPDATE/EXECUTE/CANCEL) | NOT NULL |
| old_status | VARCHAR(16) | 旧状态 | |
| new_status | VARCHAR(16) | 新状态 | |
| log_content | TEXT | 日志内容 | |
| operator | VARCHAR(64) | 操作人 | |
| created_at | DATETIME | 创建时间 | NOT NULL |

**外键**: order_biz_id -> aiw_order.biz_id  
**索引**: idx_order_created (order_biz_id, created_at)

---

## 5. AI分析域 (AI Engine Domain)

### 5.1 AI推荐信号表 (aiw_ai_signal)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 信号业务ID | PRIMARY KEY |
| signal_type | VARCHAR(32) | 信号类型 (RECOMMEND/FACTOR/SENTIMENT) | NOT NULL |
| product_biz_id | VARCHAR(64) | 产品业务ID | NOT NULL, INDEX |
| signal_value | DECIMAL(10,4) | 信号值 | |
| confidence | DECIMAL(3,2) | 置信度 (0-1) | |
| model_version | VARCHAR(32) | 模型版本 | NOT NULL |
| factors | JSON | 因子详情 | |
| explanation | TEXT | 可解释说明 | |
| valid_from | DATETIME | 生效时间 | NOT NULL |
| valid_to | DATETIME | 失效时间 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: product_biz_id -> aiw_product.biz_id  
**索引**: 
- idx_product_valid (product_biz_id, valid_from, valid_to)
- idx_signal_type (signal_type)

### 5.2 策略回测结果表 (aiw_backtest_result)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 回测业务ID | PRIMARY KEY |
| user_biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| strategy_name | VARCHAR(128) | 策略名称 | NOT NULL |
| start_date | DATE | 开始日期 | NOT NULL |
| end_date | DATE | 结束日期 | NOT NULL |
| initial_capital | DECIMAL(18,2) | 初始资金 | NOT NULL |
| final_capital | DECIMAL(18,2) | 最终资金 | NOT NULL |
| total_return | DECIMAL(6,4) | 总收益率 | |
| sharpe_ratio | DECIMAL(6,4) | 夏普比率 | |
| max_drawdown | DECIMAL(6,4) | 最大回撤 | |
| win_rate | DECIMAL(5,4) | 胜率 | |
| params | JSON | 策略参数 | |
| result_data | LONGTEXT | 详细结果数据 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: user_biz_id -> aiw_user.biz_id  
**索引**: idx_user_created (user_biz_id, created_at)

---

## 6. 风控与合规模块 (Risk & Compliance Domain)

### 6.1 风控规则表 (aiw_risk_rule)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 规则业务ID | PRIMARY KEY |
| rule_code | VARCHAR(32) | 规则编码 | UNIQUE, NOT NULL |
| rule_name | VARCHAR(128) | 规则名称 | NOT NULL |
| rule_type | VARCHAR(32) | 规则类型 (LIMIT/BLACKLIST/AML) | NOT NULL |
| rule_config | JSON | 规则配置 | NOT NULL |
| priority | INT | 优先级 | DEFAULT 0 |
| status | TINYINT | 状态 (0:禁用, 1:启用) | DEFAULT 1 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**索引**: idx_rule_code (rule_code)

### 6.2 风控检查记录表 (aiw_risk_check_log)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 检查记录ID | PRIMARY KEY |
| business_type | VARCHAR(32) | 业务类型 (ORDER/WITHDRAW) | NOT NULL |
| business_biz_id | VARCHAR(64) | 业务ID (如订单ID) | NOT NULL, INDEX |
| user_biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| rule_biz_id | VARCHAR(64) | 规则业务ID | |
| check_result | TINYINT | 检查结果 (0:拒绝, 1:通过, 2:警告) | NOT NULL |
| risk_level | TINYINT | 风险等级 (1-5) | |
| check_detail | TEXT | 检查详情 | |
| created_at | DATETIME | 创建时间 | NOT NULL |

**索引**: 
- idx_business (business_type, business_biz_id)
- idx_user_created (user_biz_id, created_at)

### 6.3 审计日志表 (aiw_audit_log)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 审计ID | PRIMARY KEY |
| user_biz_id | VARCHAR(64) | 用户业务ID | INDEX |
| action_type | VARCHAR(64) | 操作类型 | NOT NULL |
| resource_type | VARCHAR(64) | 资源类型 | |
| resource_biz_id | VARCHAR(64) | 资源业务ID | |
| request_data | JSON | 请求数据 | |
| response_data | JSON | 响应数据 | |
| ip_address | VARCHAR(64) | IP地址 | |
| user_agent | VARCHAR(256) | 用户代理 | |
| created_at | DATETIME | 创建时间 | NOT NULL |

**索引**: 
- idx_user_action (user_biz_id, action_type)
- idx_created_at (created_at)

---

## 7. 通知域 (Notification Domain)

### 7.1 通知消息表 (aiw_notification)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| biz_id | VARCHAR(64) | 通知业务ID | PRIMARY KEY |
| user_biz_id | VARCHAR(64) | 用户业务ID | NOT NULL, INDEX |
| notification_type | VARCHAR(32) | 通知类型 (ORDER/RISK/SYSTEM) | NOT NULL |
| title | VARCHAR(256) | 标题 | NOT NULL |
| content | TEXT | 内容 | NOT NULL |
| is_read | TINYINT | 是否已读 (0:未读, 1:已读) | DEFAULT 0 |
| read_at | DATETIME | 阅读时间 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**外键**: user_biz_id -> aiw_user.biz_id  
**索引**: 
- idx_user_read (user_biz_id, is_read)
- idx_created_at (created_at)

---

## 8. 系统配置域 (System Configuration Domain)

### 8.1 系统参数表 (aiw_system_config)

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY, AUTO_INCREMENT |
| config_key | VARCHAR(64) | 配置键 | UNIQUE, NOT NULL |
| config_value | TEXT | 配置值 | NOT NULL |
| config_group | VARCHAR(32) | 配置分组 | |
| description | VARCHAR(256) | 描述 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 是否删除 | DEFAULT 0 |

**索引**: idx_config_group (config_group)

---

## 9. 表关系图 (简化版)

```
aiw_user (用户)
  ├── aiw_user_preference (用户偏好)
  ├── aiw_portfolio (投资组合)
  │     ├── aiw_position (持仓) ──→ aiw_product (产品)
  │     └── aiw_order (订单) ──→ aiw_product (产品)
  │           └── aiw_order_log (订单流水)
  ├── aiw_backtest_result (回测结果)
  ├── aiw_risk_check_log (风控检查)
  ├── aiw_notification (通知)
  └── aiw_audit_log (审计日志)

aiw_product (产品)
  ├── aiw_product_metadata (产品元数据)
  ├── aiw_market_quote (行情数据)
  ├── aiw_position (持仓)
  ├── aiw_order (订单)
  └── aiw_ai_signal (AI信号)

aiw_risk_rule (风控规则)
  └── aiw_risk_check_log (风控检查)
```

---

## 10. 关键设计说明

### 10.1 GUID设计

- 所有核心业务表使用 `biz_id` (VARCHAR(64)) 作为主键，采用UUID格式
- 优势：
  - 全局唯一，便于分布式系统
  - 避免自增ID暴露业务量
  - 便于数据迁移和合并
  - 所有关联表通过 `biz_id` 建立关系，清晰统一

### 10.2 业务编号

- 对于需要用户可见的编号（如用户编号、订单编号），额外增加 `user_no`、`order_no` 等业务可读字段
- 这些字段可以按规则生成（如日期+序列号），便于客服和用户沟通

### 10.3 审计字段

- 所有表包含 `created_at`, `updated_at` 用于追踪数据变更时间
- 重要表包含 `created_by`, `updated_by` 用于追踪操作人
- 所有表包含 `is_deleted` 实现软删除，保证数据可恢复和审计完整性

### 10.4 JSON字段使用

- 对于结构化但不固定的数据（如规则配置、因子详情、策略参数），使用JSON类型
- 优势：灵活扩展，无需频繁修改表结构
- 注意：需要在应用层做好JSON schema验证

### 10.5 索引策略

- 主键索引：所有表的 `biz_id` 或 `id`
- 外键索引：所有关联字段建立索引
- 查询索引：根据常用查询条件建立联合索引
- 时间索引：涉及时间范围查询的字段建立索引

---

## 11. 后续优化方向

1. **分区表**: 行情数据、订单流水等大表可按时间分区
2. **读写分离**: 热点数据考虑读写分离架构
3. **缓存策略**: 产品信息、用户信息等高频读取数据加入Redis缓存
4. **归档机制**: 历史数据定期归档到冷存储
5. **时序数据库**: 行情数据迁移到ClickHouse/TimescaleDB提升查询性能
6. **分库分表**: 用户量增长后考虑按用户ID分库分表

---

## 12. MVP阶段建表优先级

**Phase 1 (必须)**:
- aiw_user
- aiw_product
- aiw_portfolio
- aiw_position
- aiw_order
- aiw_order_log

**Phase 2 (重要)**:
- aiw_market_quote
- aiw_user_preference
- aiw_notification
- aiw_audit_log

**Phase 3 (增强)**:
- aiw_ai_signal
- aiw_backtest_result
- aiw_risk_rule
- aiw_risk_check_log
- aiw_product_metadata
- aiw_news_article

---

**文档版本**: v1.0  
**最后更新**: 2026-05-30  
**维护者**: AI理财平台技术团队
