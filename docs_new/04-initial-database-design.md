# DZCOM 初版数据库设计

本文档定义项目初版数据库基线，用于支撑后续用户、产品、市场、组合、订单、AI 与风控等模块的逐步落地。

## 1. 设计原则

- 统一表前缀：所有业务表使用 `aiw_` 前缀。
- 统一业务主键：核心业务实体统一使用 `biz_id`。
- 审计友好：重要表默认保留时间字段和操作人字段。
- 软删除可控：仅在确实需要保留历史时引入 `is_deleted`。
- 先事务后分析：MVP 阶段先以 MySQL 落地核心事务数据，后续再按需要拆分时序或分析存储。

## 2. 通用字段约定

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 业务主键，UUID 字符串 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |
| `created_by` | 创建人或系统标识 |
| `updated_by` | 更新人或系统标识 |
| `is_deleted` | 逻辑删除标记，默认 `0` |

## 3. 初版核心表

### 3.1 账户域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_user` | 用户基础信息 | `biz_id`, `username`, `email`, `phone`, `password_hash`, `status`, `risk_level` |
| `aiw_user_preference` | 用户偏好配置 | `id`, `biz_id`, `preference_key`, `preference_value` |

### 3.2 产品域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_product` | 产品基础信息 | `biz_id`, `product_code`, `product_name`, `product_type`, `status`, `fee_rate` |
| `aiw_product_metadata` | 产品扩展属性 | `id`, `biz_id`, `metadata_key`, `metadata_value`, `data_date` |

### 3.3 市场数据域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_market_quote` | 行情快照与历史序列 | `id`, `product_biz_id`, `quote_time`, `open_price`, `close_price`, `volume` |
| `aiw_news_article` | 新闻公告与舆情基础数据 | `biz_id`, `title`, `source`, `publish_time`, `sentiment_score` |

说明：

- `aiw_market_quote` 在数据量增长后优先迁移到时序或分析型存储。

### 3.4 组合与订单域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_portfolio` | 投资组合主表 | `biz_id`, `user_biz_id`, `portfolio_name`, `total_asset`, `available_cash`, `status` |
| `aiw_position` | 持仓明细 | `biz_id`, `portfolio_biz_id`, `product_biz_id`, `quantity`, `avg_cost`, `market_value` |
| `aiw_order` | 订单主表 | `biz_id`, `order_no`, `user_biz_id`, `portfolio_biz_id`, `product_biz_id`, `order_type`, `status` |
| `aiw_order_log` | 订单状态流水 | `id`, `order_biz_id`, `action_type`, `action_result`, `remark`, `created_at` |

### 3.5 AI 与风控域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_ai_signal` | AI 推荐信号 | `biz_id`, `user_biz_id`, `product_biz_id`, `signal_type`, `confidence`, `content` |
| `aiw_risk_check_log` | 风控检查记录 | `biz_id`, `user_biz_id`, `order_biz_id`, `rule_code`, `check_result`, `risk_level` |

### 3.6 通知与审计域

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `aiw_notification` | 通知消息 | `biz_id`, `user_biz_id`, `channel`, `title`, `content`, `status` |
| `aiw_audit_log` | 审计日志 | `biz_id`, `operator_id`, `module_code`, `action_code`, `target_biz_id`, `payload` |

## 4. 关键关系

- `aiw_user` 1:N `aiw_user_preference`
- `aiw_user` 1:N `aiw_portfolio`
- `aiw_portfolio` 1:N `aiw_position`
- `aiw_product` 1:N `aiw_market_quote`
- `aiw_product` 1:N `aiw_position`
- `aiw_portfolio` 1:N `aiw_order`
- `aiw_order` 1:N `aiw_order_log`

## 5. 索引原则

- 所有 `biz_id` 主键唯一索引。
- 所有外键字段建立普通索引。
- 高频查询组合字段建立联合索引，例如：
  - `user_biz_id + status`
  - `product_biz_id + quote_time`
  - `portfolio_biz_id + product_biz_id`

## 6. 初版迁移策略

- 所有结构变更通过 Flyway 管理。
- 初版迁移按业务域拆分，但保持可按版本顺序直接执行。
- 文档先定义数据库边界，迁移脚本随后与本文档对齐整理。

## 7. 后续扩展方向

- 市场行情迁移到 ClickHouse 或 TimescaleDB。
- AI 信号、回测、策略结果拆出独立分析表。
- 风控规则、系统配置与数据源管理形成独立域表。

