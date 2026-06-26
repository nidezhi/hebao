# Kafka 配置驱动投资任务

> 2026-06-26 更新：`INVESTMENT_NEWS_COLLECTION`、RSS/fallback 和手工 endpoint 字段路径已退出自动闭环默认主链路。当前默认入口是 `AI_DATA_SOURCE_DISCOVERY`；旧任务仅作为人工审核后的执行原语或历史兼容说明。

## 目标

投资资讯、热门方向收益、市场动量和资讯热度任务采用“配置生成调度 + Kafka 解耦执行”的结构。Cron、时区、数据源、主题产品和回看窗口均由配置提供，禁止在处理器中硬编码执行频率。

## 本地 Kafka

项目根目录提供 `compose.yaml`，使用 Kafka 4 KRaft 单节点模式：

```bash
docker compose up -d kafka
docker compose ps kafka
```

应用默认连接已有本地 Kafka 的 `localhost:9092`，可通过
`KAFKA_BOOTSTRAP_SERVERS` 覆盖。项目备用 Compose 为避免与已有 Broker
冲突，默认暴露 `localhost:19092`；使用备用 Broker 时设置：

```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:19092
```

## 执行链路

1. `ConfigurableInvestmentTaskScheduler` 读取 `investment.tasks.definitions`。
2. 每个启用定义根据 Cron 动态注册，不使用 `@Scheduled`。
3. 到达触发时间后向 `dzcom.investment.task.trigger.v1` 发布 JSON 事件。
4. Kafka 消费者以 `eventId` 检查执行幂等。
5. 消费者使用 `InvestmentTaskHandler.supports` 匹配唯一处理器。
6. 任务执行结果写入 `aiw_scheduled_task_execution`。
7. 收益、动量和热度结果写入 `aiw_investment_theme_snapshot`。
8. 资讯采集结果写入既有 `aiw_news_article`。

## 首批任务

| 类型 | 作用 | 主要参数 |
| --- | --- | --- |
| `INVESTMENT_NEWS_COLLECTION` | 采集配置的 RSS/Atom 投资资讯 | `feedUrls`、`sourceCode`、`languageCode`、`maxItems` |
| `HOT_THEME_RETURN` | 计算热门方向窗口平均收益 | `windowMinutes`、`themes` |
| `MARKET_MOMENTUM_SCAN` | 结合平均收益和上涨广度计算动量 | `windowMinutes`、`themes` |
| `NEWS_HEAT_AGGREGATION` | 按关键词统计主题资讯热度 | `windowMinutes`、`themes` |

主题格式：

```text
AI人工智能=AAPL,MSFT,NVDA;半导体=NVDA,AMD,TSM;黄金=GLD
```

## 前端接口

任务能力通过 `InvestmentTaskController` 暴露，统一使用 POST 和
`Result<T>` 返回包装：

| 路径 | 作用 |
| --- | --- |
| `/api/investment/tasks/definitions` | 查询当前生效任务配置、Cron、时区和参数 |
| `/api/investment/tasks/trigger` | 按任务编码手动触发一次 Kafka 任务事件 |
| `/api/investment/tasks/executions/list` | 分页查询任务执行记录、状态、摘要和失败原因 |
| `/api/investment/tasks/articles/list` | 分页查询采集入库的投资资讯 |
| `/api/investment/tasks/snapshots/list` | 分页查询热门方向收益、动量和资讯热度快照 |

接口只使用 `interfaces/request/task` 和 `interfaces/dto/response/task`
对象，不直接暴露 Domain、持久化实体或应用层分页对象。Swagger 注解必须描述
筛选条件、分页、排序字段和返回模型，便于前端直接按接口文档接入。

## 配置原则

- 本地和开发环境默认启用，可设置 `INVESTMENT_TASKS_ENABLED=false` 关闭。
- RSS 地址通过 `INVESTMENT_NEWS_FEEDS` 配置。
- 热门方向通过 `HOT_INVESTMENT_THEMES` 配置，业务变化不需要修改 Java。
- 任务失败必须写执行记录并记录完整异常堆栈。
- 新增任务必须新增独立处理器，不得把多个任务逻辑聚合到统一工厂类。
- 新增持久化对象必须具有独立 Store、Impl、Mapper Java 和 Mapper XML。
- 新增前端可见任务能力必须同步提供接口、请求 DTO、响应 DTO 和 Swagger 文档。

## 数据说明

收益和动量仅基于平台已采集行情计算，不承诺投资结果。主题快照保存样本明细、统计窗口和计算指标，确保结果可解释、可追溯。
