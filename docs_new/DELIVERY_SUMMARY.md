# AI理财平台数据库设计交付清单

## 📋 完成内容

### 1. 数据库表结构设计文档
- **文件位置**: `/docs_new/database_schema_design.md`
- **内容**: 完整的表结构设计，包含15张核心业务表
- **特点**: 
  - 统一使用 `aiw_` 前缀（AI Wealth缩写）
  - 基于GUID (`biz_id`) 的业务关联设计
  - 按8个业务域分组

### 2. SQL迁移脚本（按执行顺序编号）

#### 📁 目录结构
```
src/main/resources/db/migration/
├── README.md                                          # 使用说明
├── V1__core_tables_user_product.sql                  # Phase 1: 用户、产品 (3张表)
├── V2__portfolio_and_orders.sql                      # Phase 1: 投资组合与订单 (4张表)
├── V3__market_data_and_notifications.sql             # Phase 2: 市场数据与通知 (3张表)
├── V4__risk_and_compliance.sql                       # Phase 2: 风控与合规 (3张表)
└── V5__ai_engine_and_advanced.sql                    # Phase 3: AI分析与高级特性 (4张表)
```

#### 📊 表清单（共17张表）

| 业务域 | 表名 | 说明 | 优先级 |
|--------|------|------|--------|
| **用户域** | aiw_user | 用户基础表 | Phase 1 |
| | aiw_user_preference | 用户偏好表 | Phase 2 |
| **产品域** | aiw_product | 产品基础表 | Phase 1 |
| | aiw_product_metadata | 产品元数据表 | Phase 3 |
| **市场数据域** | aiw_market_quote | 行情数据表 | Phase 2 |
| | aiw_news_article | 新闻公告表 | Phase 3 |
| **组合与投资域** | aiw_portfolio | 投资组合表 | Phase 1 |
| | aiw_position | 持仓明细表 | Phase 1 |
| | aiw_order | 订单表 | Phase 1 |
| | aiw_order_log | 订单流水表 | Phase 1 |
| **AI分析域** | aiw_ai_signal | AI推荐信号表 | Phase 3 |
| | aiw_backtest_result | 策略回测结果表 | Phase 3 |
| **风控与合规** | aiw_risk_rule | 风控规则表 | Phase 3 |
| | aiw_risk_check_log | 风控检查记录表 | Phase 2 |
| | aiw_audit_log | 审计日志表 | Phase 2 |
| **通知域** | aiw_notification | 通知消息表 | Phase 2 |
| **系统配置** | aiw_system_config | 系统参数表 | Phase 3 |

## 🎯 设计亮点

### 1. 统一GUID设计
- 所有核心业务表使用 `biz_id` (VARCHAR(64)) 作为主键
- UUID格式，全局唯一，便于分布式系统
- 避免自增ID暴露业务量

### 2. 扁平化文件组织
- 所有SQL文件放在同一目录下，通过数字前缀排序
- 使用Flyway命名规范（V{version}__description.sql）
- 便于版本管理和自动化工具识别
- 支持分阶段实施（Phase 1/2/3）

### 3. 完整审计机制
- 所有表包含 `created_at`, `updated_at`
- 重要表包含 `created_by`, `updated_by`
- 软删除设计（`is_deleted`）

### 4. 外键约束保障
- 通过外键确保数据完整性
- 级联删除（ON DELETE CASCADE）
- 清晰的表关系链

### 5. 索引优化
- 主键索引：所有表的 `biz_id` 或 `id`
- 外键索引：所有关联字段
- 查询索引：常用查询条件的联合索引

## 📝 执行建议

### Phase 1 - MVP必须（核心交易能力）
```bash
# Flyway会自动按版本号顺序执行，或手动按顺序执行
1. V1__core_tables_user_product.sql
2. V2__portfolio_and_orders.sql
```

**包含表**: aiw_user, aiw_product, aiw_portfolio, aiw_position, aiw_order, aiw_order_log

### Phase 2 - 重要功能（数据与风控）
```bash
3. V3__market_data_and_notifications.sql
4. V4__risk_and_compliance.sql
```

**包含表**: aiw_market_quote, aiw_news_article, aiw_notification, aiw_risk_rule, aiw_risk_check_log, aiw_audit_log

### Phase 3 - 增强功能（AI与高级特性）
```bash
5. V5__ai_engine_and_advanced.sql
```

**包含表**: aiw_ai_signal, aiw_backtest_result, aiw_product_metadata, aiw_system_config

## 🔧 技术规格

- **数据库**: MySQL 8.0+ / MariaDB 10.5+
- **引擎**: InnoDB
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci (推荐)
- **外键**: 启用（ON DELETE CASCADE）
- **索引**: B-Tree索引

## ⚠️ 注意事项

1. **执行前检查**
   - 确认数据库字符集为 `utf8mb4`
   - 建议在测试环境先验证
   - 生产环境执行前务必备份

2. **Flyway集成**（可选）
   - SQL文件已按Flyway命名规范（V1__xxx.sql）
   - 如需多版本迭代，递增版本号（V2__, V3__...）
   - 在 `application.yaml` 中配置Flyway

3. **性能优化**
   - 行情数据量大时考虑分区表
   - 高频读取数据加入Redis缓存
   - 历史数据定期归档

4. **后续扩展**
   - 时序数据迁移到ClickHouse/TimescaleDB
   - 用户量增长后考虑分库分表
   - 读写分离架构

## 📚 相关文档

- [业务架构设计](./ai_wealth_architecture.md)
- [数据库表结构设计](./database_schema_design.md)
- [SQL迁移脚本说明](../src/main/resources/db/migration/README.md)

---

**交付日期**: 2026-05-30  
**版本**: v1.0  
**维护团队**: AI理财平台技术团队
