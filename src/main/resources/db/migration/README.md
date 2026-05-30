# AI Wealth Platform - Database Migration Scripts

## 目录结构说明

本目录包含AI理财平台（AI Wealth Platform）的数据库迁移脚本，按执行顺序通过数字前缀排序。

### 表命名规范

所有表使用 `aiw_` 前缀（AI Wealth的缩写），例如：
- `aiw_user` - 用户表
- `aiw_product` - 产品表
- `aiw_order` - 订单表

### SQL文件列表（按执行顺序）

```
db/migration/
├── README.md                                          # 使用说明
├── V1__core_tables_user_product.sql                  # Phase 1: 用户、产品基础表 (3张)
├── V2__portfolio_and_orders.sql                      # Phase 1: 投资组合与订单 (4张)
├── V3__market_data_and_notifications.sql             # Phase 2: 市场数据与通知 (3张)
├── V4__risk_and_compliance.sql                       # Phase 2: 风控与合规 (3张)
└── V5__ai_engine_and_advanced.sql                    # Phase 3: AI分析与高级特性 (4张)
```

### 执行顺序

SQL文件通过 `V{version}__` 前缀控制执行顺序，Flyway等工具会自动按版本号顺序执行：

**Phase 1 (MVP必须)**:
1. `V1__core_tables_user_product.sql` - 用户表、产品表
2. `V2__portfolio_and_orders.sql` - 投资组合、持仓、订单表

**Phase 2 (重要功能)**:
3. `V3__market_data_and_notifications.sql` - 行情、新闻、通知表
4. `V4__risk_and_compliance.sql` - 风控规则、审计日志表

**Phase 3 (增强功能)**:
5. `V5__ai_engine_and_advanced.sql` - AI信号、回测、系统配置表

### 核心表关系

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

### 设计特点

1. **统一GUID**: 所有核心业务表使用 `biz_id` (UUID) 作为主键
2. **外键约束**: 通过外键确保数据完整性（ON DELETE CASCADE）
3. **审计字段**: 所有表包含 `created_at`, `updated_at`, `is_deleted`
4. **索引优化**: 为常用查询字段建立索引
5. **字符集**: 使用 `utf8mb4` 支持完整Unicode字符
6. **版本管理**: 使用Flyway命名规范（V{version}__description.sql）

### 注意事项

- 执行前请确认数据库字符集为 `utf8mb4`
- 建议在测试环境先验证SQL脚本
- 生产环境执行前务必备份数据
- Flyway会自动按版本号顺序执行，无需手动干预
- 如需新增迁移脚本，递增版本号（如 V6__xxx.sql）

### 技术栈

- 数据库: MySQL 8.0+ / MariaDB 10.5+
- 引擎: InnoDB
- 字符集: utf8mb4
- 排序规则: utf8mb4_unicode_ci (推荐)
- 迁移工具: Flyway (推荐) 或手动按顺序执行

---

**文档版本**: v2.0  
**最后更新**: 2026-05-30  
**维护者**: AI理财平台技术团队
