# 本地业务数据重置与初始化数据

## 1. 文档目的

本文档记录本地/开发环境清空业务数据并注入高质量演示数据的方式。

本方案用于前端重构、后端联调和闭环验收，不作为生产迁移，不进入 Flyway 自动执行链路。

## 2. 执行脚本

脚本路径：

```text
scripts/local/reset-and-seed-investment-demo.sql
```

执行边界：

- 仅允许在数据库名为 `dz_database` 的本地/开发库执行。
- 脚本开头会校验 `DATABASE() = 'dz_database'`，不满足时直接中断。
- 使用 `DELETE` 按业务依赖顺序清空数据，保留 `flyway_schema_history`、角色和权限基线。
- 脚本不是 Flyway 迁移，禁止放入 `src/main/resources/db/migration/`。

推荐执行命令：

```bash
mysql -uroot dz_database --execute="source scripts/local/reset-and-seed-investment-demo.sql"
```

如本地数据库有密码，请加 `-p` 或使用本机安全配置。

## 3. 默认演示账号

| 账号 | 密码 | 角色 | 用途 |
| --- | --- | --- | --- |
| `demo_admin` | `Demo@123456` | `ADMIN` | 后台、任务、数据源、Prompt 和风控审计联调 |
| `demo_investor` | `Demo@123456` | `USER` | 用户端报告、Mock 组合和反馈联调 |

密码以 BCrypt 哈希写入 `aiw_user_credential`，脚本不保存明文凭据以外的敏感配置。

## 4. 初始化数据范围

脚本会注入以下稳定样本：

| 模块 | 初始化内容 |
| --- | --- |
| 账户 | 管理员和普通投资人、风险画像、角色和偏好 |
| 数据源治理 | `CSRC`、`SSE`、`SZSE`、`CNINFO`、`CHINA_WEALTH`、`EASTMONEY`、`WIND`、`CHOICE` |
| 定时任务 | L1监管披露、L1公告、L2理财净值、动量、收益、资讯热度、自动报告 |
| 产品池 | AI、半导体、黄金、银行理财等 10 个产品 |
| 产品画像 | 风险、资产类别、波动、流动性、Mock 可交易开关 |
| 行情净值 | 场内 ETF/股票 1D 行情和银行理财 1D 净值 |
| 资讯公告 | 监管、公告、理财净值、黄金资讯样本 |
| 主题证据 | 资讯-主题-产品关系和主题快照 |
| AI 治理 | OpenAI 兼容默认模型、本地规则模型、Prompt 模板、变量和输出 Schema |
| 投资报告 | AI 主题高可信报告、黄金中可信观察报告 |
| Mock 交易 | 模拟组合、估值、持仓、成交订单和拒单 |
| 风控审计 | 数据质量通过、风险匹配、产品不可 Mock、现金缓冲等记录 |
| 回测反馈 | 回测结果、用户观察/拒绝反馈、Prompt 评估 |

## 5. 前端可直接联调的重点入口

前端重构后可以直接围绕这些页面验收：

- 数据源看板：查看数据源、健康状态和质量快照。
- 任务中心：查看 7 个默认任务，验证参数下拉项和 JSON 配置。
- 产品池：查看产品主档、投资画像、主题关系和净值行情。
- 投资机会/报告：查看报告可信等级、质量门禁、趋势图和 Prompt 快照。
- Mock 组合：查看组合估值、持仓、成交和拒单。
- 风控审计：查看质量不足、风险等级、现金、产品 Mock 开关等审计记录。
- 反馈闭环：查看报告、Prompt、模型、回测、用户采纳/拒绝之间的链路。

## 6. 数据质量说明

初始化数据不是生产级真实行情，也不构成投资建议。它的目标是提供稳定、高质量结构样本：

- 关键报告使用 `HIGH_CONFIDENCE` 或 `MEDIUM_CONFIDENCE` 并带 `data_quality_gate`。
- L4 资讯只作为交叉验证或弱信号，不单独驱动配置建议。
- 单一股票 `688981` 被标记为不可 Mock，用于验证风控拦截。
- `WIND`、`CHOICE` 默认禁用，用于展示供应商授权占位。

## 7. 验收 SQL

脚本末尾会输出核心表行数。也可以手动检查：

```sql
SELECT COUNT(*) FROM aiw_product;
SELECT COUNT(*) FROM aiw_market_quote;
SELECT COUNT(*) FROM aiw_investment_analysis_report;
SELECT COUNT(*) FROM aiw_risk_check;
SELECT COUNT(*) FROM aiw_investment_feedback;
```

预期至少包含：

- 产品：10 条
- 行情/净值：38 条
- 投资报告：2 条
- 风控检查：4 条
- 用户反馈：2 条
