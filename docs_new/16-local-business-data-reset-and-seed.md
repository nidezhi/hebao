# 本地业务数据清空与真实采集启动

## 1. 文档目的

本文档记录本地/开发环境清空投资业务数据，并启动真实数据采集闭环的方式。

本方案不再注入伪造的产品、行情、资讯、报告、Mock 交易、回测或反馈。用户信息可以保留为本地联调 Mock，但业务数据必须由定时任务、真实端点、授权供应商和后续用户动作逐步产生。

## 2. 执行脚本

脚本路径：

```text
scripts/local/reset-business-and-start-real-ingestion.sql
```

执行边界：

- 仅允许在数据库名为 `dz_database` 的本地/开发库执行。
- 脚本开头会校验 `DATABASE() = 'dz_database'`，不满足时中断。
- 清空业务结果表和采集产物，保留/重建 demo 用户、数据源、任务、模型和 Prompt 基线。
- 脚本不是 Flyway 迁移，禁止放入 `src/main/resources/db/migration/`。

推荐执行命令：

```bash
mysql -uroot dz_database --execute="source scripts/local/reset-business-and-start-real-ingestion.sql"
```

如果使用本项目的 JDBC 执行器：

```bash
javac --add-modules java.sql -cp ~/.m2/repository/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar -d /private/tmp scripts/local/JdbcSqlScriptRunner.java
java --add-modules java.sql -cp /private/tmp:~/.m2/repository/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar JdbcSqlScriptRunner config/application-secrets.yaml scripts/local/reset-business-and-start-real-ingestion.sql
```

## 3. 默认联调账号

| 账号 | 密码 | 角色 | 用途 |
| --- | --- | --- | --- |
| `demo_admin` | `Demo@123456` | `ADMIN` | 后台、任务、数据源、Prompt 和风控审计联调 |
| `demo_investor` | `Demo@123456` | `USER` | 用户端报告、Mock 组合和反馈联调 |

账号只用于本地联调。投资产品、行情、资讯、报告和交易结果不再通过脚本伪造。

## 4. 清空后保留/初始化内容

| 模块 | 初始化内容 | 说明 |
| --- | --- | --- |
| 账户 | demo 管理员、demo 投资人、风险画像、角色和偏好 | 允许 Mock 用户 |
| 数据源治理 | `CSRC`、`SSE`、`SZSE`、`CNINFO`、`CHINA_WEALTH`、`EASTMONEY`、`WIND`、`CHOICE` | 健康状态为“等待首次真实采集” |
| 定时任务 | L1监管披露、L1公告、L2理财净值、动量、收益、资讯热度、自动报告、自动 Prompt 治理 | 前端可配置 Cron、端点、字段路径和模型 |
| AI 模型 | `local-rule-analysis`、`openai-compatible-analysis` | OpenAI 默认 `mockEnabled=true`，前端配置 Key 后可切真实调用 |
| Prompt 基线 | `investment-plan-from-report:auto-v1` | 系统配置资产，不是业务报告产物 |

## 5. 清空后必须为 0 的业务数据

| 表 | 说明 |
| --- | --- |
| `aiw_product` | 产品池等待 `WEALTH_PRODUCT_NAV_REFRESH` 等真实采集 upsert |
| `aiw_market_quote` | 行情/净值等待真实采集写入 |
| `aiw_news_article` | 资讯、公告、监管披露等待真实端点写入 |
| `aiw_investment_analysis_report` | 报告等待自动报告任务基于真实输入生成 |
| `aiw_portfolio` / `aiw_order` / `aiw_position` | Mock 交易等待用户或报告方案触发 |
| `aiw_risk_check` | 风控审计等待真实业务动作触发 |
| `aiw_backtest_result` / `aiw_investment_feedback` / `aiw_ai_prompt_evaluation` | 反馈闭环等待报告、Mock、回测和用户反馈产生 |

## 6. 真实采集闭环

清空后预期链路如下：

```text
定时任务配置
  -> L1/L2/L3/L4 数据源真实采集
  -> 产品池 upsert 与净值行情入库
  -> 资讯、公告、监管披露入库
  -> 数据源健康和质量快照
  -> 自动投资报告
  -> 自动 Prompt 治理
  -> 用户前端采纳/拒绝
  -> Mock 交易、回测、风控审计和 Prompt 评估
```

端点未配置、外部源失败或返回空数据时，任务只写健康失败和执行摘要，不写兜底业务数据。

## 7. 验收 SQL

只读验收脚本：

```text
scripts/local/verify-real-ingestion-baseline.sql
```

关键预期：

- 用户为 2 条。
- 数据源为 8 条。
- 定时任务为 8 条。
- Prompt 基线为 1 条。
- 产品、行情、资讯、报告、组合、订单、风控检查、回测、反馈和 Prompt 评估均为 0。

执行真实采集任务后，相关业务表会自然增长；增长来源必须可通过任务执行记录、数据源健康和质量快照追溯。
