# 第一批确定性真实数据采集器方案

生成时间：2026-06-27  
目标：先把 `products / quotes / news` 从 0 变成可持续增长、可追溯、可校验的数据资产，再恢复 AI 报告和自动闭环。

## 方案结论

第一批采集器不再依赖大模型采集真实数据。

大模型只允许参与：

- 推荐新数据源
- 解释采集失败
- 生成字段映射候选
- 总结数据质量缺口

真实落库必须由确定性采集器完成：

- 产品池写入 `aiw_product`
- 行情/净值写入 `aiw_market_quote`
- 资讯/公告写入 `aiw_news_article`
- 数据源健康写入 `aiw_data_source_health`
- 数据质量写入 `aiw_data_quality_snapshot`

## 第一批目标

先只跑通小而硬的一组资产，不做全市场。

### 主题范围

沿用当前闭环配置中的三个主题：

| 主题 | 标的代码 |
| --- | --- |
| AI 人工智能 | `159819`, `588000`, `515980` |
| 半导体 | `512480`, `159995`, `688981` |
| 黄金 | `518880`, `159934` |

### 资产范围

第一批只覆盖：

- ETF
- A 股股票
- 主题相关新闻
- 交易所/监管公告

暂不覆盖：

- 银行理财
- 私募
- 债券深度行情
- 研报全文
- 海外资产

理由：第一批目标不是做全，而是做稳定。先让闭环有真实、可追溯、可复跑的最小数据资产。

## 推荐数据源策略

### 国内 AI 能力层：通义千问

如果 OpenAI 当前无法联网或对国内投资数据理解/检索不稳定，国内投资数据的“信息整理、研报解读、宏观分析、投教陪伴”优先挂靠通义千问。

边界必须清晰：

- 千问可以负责联网信息整理、中文金融语境理解、研报/公告摘要、宏观解释、投教问答。
- 千问不直接替代行情落库采集器。
- 行情、K 线、净值等结构化数据仍由 AKShare / Tushare / 授权行情源确定性写入数据库。
- 千问输出如果参与报告，必须引用已落库数据或可追溯来源。

建议新增一个正式模型配置：

```json
{
  "modelCode": "qwen-domestic-investment",
  "modelVersion": "default-v1",
  "provider": "OPENAI_COMPATIBLE",
  "baseUrl": "https://dashscope.aliyuncs.com/compatible-mode/v1",
  "model": "qwen-plus",
  "secretRef": "QWEN_API_KEY",
  "mockEnabled": false,
  "temperature": 0.2,
  "timeoutSeconds": 90,
  "maxTokens": 4000
}
```

配置说明：

| 字段 | 说明 |
| --- | --- |
| baseUrl | 阿里云百炼 / DashScope OpenAI 兼容模式地址 |
| model | 默认 `qwen-plus`，后续可按前端配置切换 |
| secretRef | `QWEN_API_KEY`，密钥只放在 secrets 或环境变量 |
| mockEnabled | 必须为 `false` |
| timeoutSeconds | 国内联网/长文分析建议 90 秒起 |
| maxTokens | 第一批建议限制 4000，避免成本失控 |

建议场景绑定：

| 场景 | 默认模型 | 说明 |
| --- | --- | --- |
| `DOMESTIC_DATA_INTELLIGENCE` | `qwen-domestic-investment` | 国内投资数据整理、来源解释、字段映射建议 |
| `RESEARCH_REPORT_READING` | `qwen-domestic-investment` | 财报、公告、研报提炼 |
| `MACRO_ANALYSIS` | `qwen-domestic-investment` | 宏观数据解释和影响链路分析 |
| `INVESTOR_EDUCATION_COMPANION` | `qwen-domestic-investment` | 用户陪伴、投教、模拟盘解释 |
| `AUTO_REPORT_GENERATION` | 可配置 | 国内市场报告可优先千问，OpenAI 作为备选 |

### 千问挂靠 Skills

以下 Skill 是能力分工，不等于都直接写核心业务表。

#### 1. 行情数据 Skill

Skill Code：`MARKET_DATA_TOOL_SKILL`

职责：

- 对接 AKShare 或 Tushare。
- 负责获取实时股价、日线 K 线、ETF 净值、大盘指数。
- 输出字段映射和质量解释。
- 实际落库由确定性采集器执行。

边界：

- 不允许千问凭记忆生成行情。
- 没有 AKShare/Tushare 返回值时，必须输出数据缺口。

#### 2. 研报解读 Skill

Skill Code：`RESEARCH_REPORT_READING_SKILL`

职责：

- 对接巨潮资讯网公告、财报或第三方研报库。
- 提炼上市公司的核心财务指标、风险提示、经营变化、行业趋势。
- 输出结构化摘要，供投资报告引用。

边界：

- 原文 URL、公告编号或研报来源必须可追溯。
- 不直接给买卖建议，只提供证据摘要和风险解释。

#### 3. 宏观分析 Skill

Skill Code：`MACRO_ANALYSIS_SKILL`

职责：

- 对接 FRED 等宏观数据库。
- 分析 CPI、利率、美元指数、国债收益率、流动性等指标对市场风格的影响。
- 输出宏观因子解释和风险情景。

边界：

- FRED 偏海外宏观，国内宏观后续可扩展国家统计局、央行、海关总署等来源。
- 宏观结论必须作为背景解释，不直接覆盖产品级信号。

#### 4. 用户陪伴 / 投教 Skill

Skill Code：`INVESTOR_EDUCATION_COMPANION_SKILL`

职责：

- 解答新手投资概念。
- 解释模拟盘操作。
- 解释报告中的风险等级、回撤、波动率、仓位、止损等概念。
- 帮助用户理解为什么当前系统阻断报告或不建议交易。

边界：

- 不输出确定性收益承诺。
- 不诱导真实交易。
- 用户风险等级不匹配时必须提示风险。

### 主数据源：AKShare HTTP Sidecar

采用 AKShare + AKTools HTTP 部署作为第一批行情/资讯聚合入口。

原因：

- AKShare 官方文档覆盖股票、公募基金、指数、宏观、新闻等多类财经数据。
- AKShare 支持 HTTP 部署，后端 Java 服务可以通过 HTTP 调用，不需要把 Python 嵌入 JVM。
- 采集器逻辑可以保持确定性：固定接口、固定字段映射、固定质量规则。

部署形态：

```text
Spring Boot 后端
  -> DeterministicMarketDataCollector
      -> AKTools HTTP
          -> AKShare 数据接口
```

### 备选数据源：直接 HTTP API

当 AKShare 接口不稳定或无法部署时，允许配置直接 HTTP API：

- 东方财富行情接口
- 交易所/巨潮公告接口
- 公开 RSS/财经新闻接口
- 未来可接入 Tushare Pro、Wind、Choice 等授权数据源

但第一批实现建议先只做 AKShare Sidecar + 官方公告端点，避免一次接入太散。

## 采集器设计

第一批新增 4 个确定性任务类型。

### 1. `REAL_PRODUCT_UNIVERSE_SYNC`

职责：同步主题标的产品主档。

输入参数：

```json
{
  "marketScope": "CN_MAINLAND",
  "sourceCode": "AKSHARE",
  "themes": "AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934",
  "defaultCurrency": "CNY",
  "defaultRiskLevel": "3",
  "providerBaseUrl": "http://127.0.0.1:8080"
}
```

输出：

- upsert `aiw_product`
- upsert `aiw_product_theme_relation`
- save `aiw_data_source_health`
- save `aiw_data_quality_snapshot`

产品类型判定：

| 代码特征 | marketCode | productType |
| --- | --- | --- |
| `15xxxx` | `SZSE` | `ETF` |
| `51xxxx` / `58xxxx` | `SSE` | `ETF` |
| `60xxxx` / `68xxxx` | `SSE` | `STOCK` |
| `00xxxx` / `30xxxx` | `SZSE` | `STOCK` |

落库字段：

| 目标字段 | 来源 |
| --- | --- |
| product_code | 配置代码 |
| product_name | AKShare 查询结果，失败时使用配置别名 |
| product_type | 代码规则 + 数据源结果校验 |
| market_code | 代码规则 |
| currency | `CNY` |
| trade_status | 可查到行情则 `TRADABLE` |
| risk_level | ETF 默认 3，股票默认 4，可配置 |
| description | 来源、主题、首次采集说明 |

验收标准：

- 至少写入 8 个产品。
- 每个主题至少 2 个产品。
- 产品不可用时必须记录缺口，不允许写假产品。

### 2. `REAL_MARKET_QUOTE_SYNC`

职责：采集 ETF / A 股最新行情和日线行情。

输入参数：

```json
{
  "marketScope": "CN_MAINLAND",
  "sourceCode": "AKSHARE",
  "providerBaseUrl": "http://127.0.0.1:8080",
  "quoteInterval": "1D",
  "lookbackDays": "10",
  "themes": "AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934"
}
```

输出：

- upsert `aiw_market_quote`
- save `aiw_data_source_health`
- save `aiw_data_quality_snapshot`

行情策略：

第一优先级：

- 日线行情：保证趋势、收益率、回测可用。

第二优先级：

- 实时快照：用于驾驶舱最新状态。

第一批不做分钟线。分钟线会显著增加数据量和接口风险，等日线闭环稳定后再加。

落库字段：

| 目标字段 | 说明 |
| --- | --- |
| product_biz_id | 由 marketCode + productCode 查询产品 |
| source_code | `AKSHARE` |
| quote_interval | `1D` |
| quote_time | 交易日收盘时间或净值日期 |
| open_price | 日线开盘价，可空 |
| high_price | 日线最高价，可空 |
| low_price | 日线最低价，可空 |
| close_price | 必填，收盘价/净值 |
| previous_close_price | 上一个交易日 close |
| volume | 成交量，可空 |
| turnover_amount | 成交额，可空 |

验收标准：

- 至少 8 个产品有行情。
- 每个产品至少 2 个交易日数据。
- `close_price` 缺失的行情不得落库。
- 质量分低于阈值时不允许继续报告生成。

### 3. `REAL_NEWS_SYNC`

职责：采集主题相关新闻、市场新闻和政策新闻。

输入参数：

```json
{
  "marketScope": "CN_MAINLAND",
  "sourceCode": "AKSHARE_NEWS",
  "providerBaseUrl": "http://127.0.0.1:8080",
  "keywords": "AI,人工智能,算力,大模型,半导体,芯片,集成电路,黄金,金价,贵金属",
  "freshnessHours": "72",
  "maxItems": "100"
}
```

输出：

- upsert `aiw_news_article`
- save `aiw_news_target` 或后续 `aiw_news_article_relation`
- save `aiw_data_source_health`
- save `aiw_data_quality_snapshot`

落库字段：

| 目标字段 | 说明 |
| --- | --- |
| external_id | 来源 ID，没有则用 URL hash |
| article_type | `NEWS` |
| title | 必填 |
| summary | 可空 |
| content | 可空 |
| source_code | `AKSHARE_NEWS` |
| source_url | 原文 URL，尽量必填 |
| language_code | `zh-CN` |
| publish_time | 必填，缺失时不落库或降权 |

验收标准：

- 近 72 小时至少 20 条资讯。
- 每个核心主题至少命中 3 条。
- 没有标题、发布时间、来源的资讯不得进入正式报告依据。

### 4. `REAL_DISCLOSURE_SYNC`

职责：采集交易所公告、巨潮公告、监管披露。

现状：已有 `OfficialDisclosureCollectionTaskHandler` 和 `HttpOfficialDisclosureClient`，可复用。

第一批重点不是新增模型逻辑，而是给它稳定配置：

- 固定 endpoint
- 固定 itemsPath
- 固定 titlePath
- 固定 publishTimePath
- 固定 urlPath
- 固定 includeKeywords

输出：

- upsert `aiw_news_article`
- article_type 使用：
  - `ANNOUNCEMENT`
  - `REGULATORY`
- save `aiw_data_source_health`
- save `aiw_data_quality_snapshot`

验收标准：

- 至少一个官方/交易所端点可稳定返回。
- 近 7 天公告/监管数据可入库。
- 采不到时只记录缺口，不生成假公告。

## 数据质量门禁

新增一个总质量任务：`REAL_DATA_QUALITY_SNAPSHOT`

职责：综合检查产品、行情、资讯三类核心资产。

质量规则：

| 指标 | 通过标准 |
| --- | --- |
| 产品覆盖 | 配置产品覆盖率 >= 80% |
| 行情覆盖 | 有效行情产品覆盖率 >= 80% |
| 行情新鲜度 | 最新行情距当前不超过 3 个自然日，节假日允许配置放宽 |
| 行情连续性 | 每个产品至少 2 个交易日行情 |
| 资讯覆盖 | 近 72 小时资讯 >= 20 条 |
| 主题资讯覆盖 | 每个主题 >= 3 条 |
| 来源可追溯 | source_code/source_url/source_time 具备基本可追踪性 |

报告生成前置条件：

```text
products_ready = true
quotes_ready = true
news_ready = true
quality_score >= 0.60
```

不满足时：

- 不调用大模型报告。
- 不调用 Prompt 治理。
- 不调用 Mock 闭环。
- 前端只展示数据缺口和下一步建议。

## 默认任务频率

第一批不要高频。

| 任务 | 默认频率 | 是否默认开启 |
| --- | --- | --- |
| REAL_PRODUCT_UNIVERSE_SYNC | 每天 08:30 | 开启 |
| REAL_MARKET_QUOTE_SYNC | 每天 15:30、17:30 | 开启 |
| REAL_NEWS_SYNC | 每 2 小时 | 开启 |
| REAL_DISCLOSURE_SYNC | 每 6 小时 | 开启 |
| REAL_DATA_QUALITY_SNAPSHOT | 每小时 | 开启 |
| AUTO_INVESTMENT_REPORT_GENERATION | 手动/每日 20:30 | 初期关闭 |
| AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION | 手动 | 初期关闭 |
| AI_DATA_SOURCE_DISCOVERY | 手动/每周 | 关闭 |
| AI_STRUCTURED_DATA_COLLECTION | 手动实验 | 关闭 |

## 任务恢复顺序

### 第一步：只跑产品同步

验收：

```sql
SELECT COUNT(*) FROM aiw_product WHERE is_deleted = 0;
```

期望：`>= 8`

### 第二步：跑行情同步

验收：

```sql
SELECT COUNT(DISTINCT product_biz_id), COUNT(*)
FROM aiw_market_quote
WHERE quote_interval = '1D';
```

期望：

- 产品数 `>= 8`
- 行情点 `>= 16`

### 第三步：跑资讯同步

验收：

```sql
SELECT COUNT(*)
FROM aiw_news_article
WHERE is_deleted = 0
  AND publish_time >= DATE_SUB(NOW(), INTERVAL 72 HOUR);
```

期望：`>= 20`

### 第四步：跑质量快照

验收：

```sql
SELECT data_type, AVG(quality_score), SUM(sample_count)
FROM aiw_data_quality_snapshot
GROUP BY data_type;
```

期望：

- `MARKET_QUOTE` 有样本
- `NEWS` 有样本
- 综合质量分 `>= 0.60`

### 第五步：手动触发报告

只有前四步通过后，才允许调用模型生成报告。

## 前端配置要求

前端需要支持一个“真实数据采集配置”页面，不放在普通菜单管理风格里，而是作为驾驶舱里的数据资产配置区。

必须可配置：

- 数据源启用/停用
- providerBaseUrl
- sourceCode
- 主题与产品代码
- 关键词
- 任务频率
- maxItems
- freshnessHours
- 质量阈值
- 是否允许报告任务读取该数据源

驾驶舱必须展示：

- 产品覆盖率
- 行情覆盖率
- 最新行情时间
- 近 72 小时资讯数
- 数据质量分
- 当前是否允许生成报告
- 当前阻断原因

## 不做什么

第一批明确不做：

- 不接真实交易。
- 不接全市场。
- 不做分钟线。
- 不让大模型直接采集真实数据。
- 不在数据不足时生成投资建议。
- 不再默认高频跑报告/Prompt/闭环。

## 成功定义

第一批成功不是“AI 报告多漂亮”。

成功定义是：

```text
产品池 > 0
行情 > 0
资讯 > 0
质量快照 > 0
数据质量门禁可解释
报告只在数据达标时调用模型
闭环不再无效烧 token
```

达到这个状态后，项目才真正回到正轨。

## 实现优先级

1. 注入千问模型配置模板和四类 Skill 模板。
2. 新增 AKShare/AKTools HTTP 客户端。
3. 新增 `REAL_PRODUCT_UNIVERSE_SYNC`。
4. 新增 `REAL_MARKET_QUOTE_SYNC`。
5. 新增 `REAL_NEWS_SYNC`。
6. 固化 `REAL_DISCLOSURE_SYNC` 默认端点配置。
7. 新增 `REAL_DATA_QUALITY_SNAPSHOT`。
8. 修改闭环前置门禁：没有真实数据，不调用模型。
9. 恢复手动报告验证。
10. 连续 3 次成功后，再恢复低频自动闭环。

## 参考

- AKShare 文档首页说明其数据字典覆盖股票、公募基金、指数、宏观等数据类型。
- AKShare HTTP 部署说明支持通过 AKTools 以 HTTP 方式部署，Java 后端可作为普通 HTTP 服务调用。
- 阿里云百炼 / DashScope 提供 OpenAI 兼容模式，可用 `https://dashscope.aliyuncs.com/compatible-mode/v1` 作为兼容 baseUrl。
