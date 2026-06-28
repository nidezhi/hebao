package com.example.dzcom.interfaces.controller.task;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.task.InvestmentTaskScheduleRefreshPort;
import com.example.dzcom.application.service.task.InvestmentTaskManagementService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskDefinitionResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskTriggerResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentThemeSnapshotResponse;
import com.example.dzcom.interfaces.dto.response.task.NewsArticleRelationResponse;
import com.example.dzcom.interfaces.dto.response.task.NewsArticleResponse;
import com.example.dzcom.interfaces.dto.response.task.ScheduledTaskExecutionResponse;
import com.example.dzcom.interfaces.request.task.InvestmentThemeSnapshotListRequest;
import com.example.dzcom.interfaces.request.task.NewsArticleRelationListRequest;
import com.example.dzcom.interfaces.request.task.NewsArticleListRequest;
import com.example.dzcom.interfaces.request.task.SaveInvestmentTaskDefinitionRequest;
import com.example.dzcom.interfaces.request.task.TaskExecutionListRequest;
import com.example.dzcom.interfaces.request.task.TriggerInvestmentTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 投资任务配置、触发与结果查询接口。
 *
 * <p>该接口只暴露接口层 DTO，供前端查看当前任务配置、手动触发任务、
 * 追踪执行记录，并消费采集到的资讯与投资方向快照。</p>
 */
@RestController
@RequestMapping("/api/investment/tasks")
@RequiredArgsConstructor
@Tag(name = "投资任务与资讯", description = "配置驱动投资任务、Kafka 触发、资讯采集和热门方向收益查询接口")
public class InvestmentTaskController {
    private final InvestmentTaskManagementService tasks;
    private final InvestmentTaskScheduleRefreshPort scheduleRefreshPort;

    /**
     * 查询当前生效的投资任务配置。
     *
     * @return 投资任务配置列表
     * @author dz
     * @date 2026-06-16
     */
    @PostMapping("/definitions")
    @Operation(summary = "查询投资任务配置", description = "返回当前 YAML/环境变量生成的 investment.tasks.definitions 配置，便于前端展示任务类型、Cron、时区和参数。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回投资任务配置数组", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<List<InvestmentTaskDefinitionResponse>> definitions() {
        return Result.success(tasks.definitions().stream()
            .map(InvestmentTaskDefinitionResponse::from)
            .toList());
    }

    /**
     * 新增或更新投资任务配置。
     *
     * @param request 任务配置请求
     * @return 保存后的任务配置
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/definitions/save")
    @Operation(summary = "保存投资任务配置", description = "新增或更新落库的投资任务配置。保存成功后会刷新当前节点的动态 Cron 调度，参数会覆盖后续自动触发。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的任务配置", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentTaskDefinitionResponse> saveDefinition(
        @Valid @RequestBody SaveInvestmentTaskDefinitionRequest request
    ) {
        InvestmentTaskDefinitionResponse response = InvestmentTaskDefinitionResponse.from(
            tasks.saveDefinition(
                request.code(),
                request.type(),
                request.cron(),
                request.zone(),
                request.enabled(),
                normalizeParameters(request.parameters()),
                request.description()
            ));
        scheduleRefreshPort.refreshSchedules();
        return Result.success(response);
    }

    /**
     * 手动触发一次投资任务。
     *
     * @param request 任务编码和本次覆盖参数
     * @return 任务触发事件信息
     * @throws BusinessException 当任务配置不存在时抛出
     * @author dz
     * @date 2026-06-16
     */
    @PostMapping("/trigger")
    @Operation(summary = "手动触发投资任务", description = "按任务编码发布 Kafka 任务触发事件。parameters 仅覆盖本次执行，不修改 YAML 配置。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "触发成功，返回 Kafka 事件 ID", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "任务配置不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentTaskTriggerResponse> trigger(
        @Valid @RequestBody TriggerInvestmentTaskRequest request
    ) {
        return Result.success(InvestmentTaskTriggerResponse.from(
            tasks.trigger(request.taskCode(), normalizeParameters(request.parameters()), "MANUAL")));
    }

    /**
     * 分页查询任务执行记录。
     *
     * @param request 执行记录筛选和分页请求
     * @return 执行记录分页结果
     * @author dz
     * @date 2026-06-16
     */
    @PostMapping("/executions/list")
    @Operation(summary = "分页查询投资任务执行记录", description = "按任务编码、任务类型、状态和开始时间区间查询执行记录，用于前端展示任务运行状态与失败原因。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回执行记录分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<ScheduledTaskExecutionResponse>> executions(
        @Valid @RequestBody TaskExecutionListRequest request
    ) {
        return Result.success(PageResponse.from(tasks.executions(
            request.taskCode(),
            request.taskType(),
            request.status(),
            request.startedFrom(),
            request.startedTo(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "startedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), ScheduledTaskExecutionResponse::from));
    }

    /**
     * 分页查询已采集的投资资讯。
     *
     * @param request 资讯筛选和分页请求
     * @return 资讯分页结果
     * @author dz
     * @date 2026-06-16
     */
    @PostMapping("/articles/list")
    @Operation(summary = "分页查询投资资讯", description = "查询定时任务采集入库的资讯，可按关键字、类型、来源、语言和发布时间区间筛选。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回资讯分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<NewsArticleResponse>> articles(
        @Valid @RequestBody NewsArticleListRequest request
    ) {
        return Result.success(PageResponse.from(tasks.articles(
            request.keyword(),
            request.articleType(),
            request.sourceCode(),
            request.languageCode(),
            request.publishFrom(),
            request.publishTo(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "publishTime" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), NewsArticleResponse::from));
    }

    /**
     * 将前端自然 JSON 参数转换为任务引擎统一使用的字符串参数。
     *
     * <p>任务定义表中的 parameters 是 JSON 对象，但任务处理器以
     * {@code Map<String, String>} 读取参数。前端传入对象或数组时，这里序列化为
     * JSON 字符串，避免字段映射、候选配置等结构化参数在反序列化阶段失败。</p>
     *
     * @param parameters 前端传入的任务参数
     * @return 任务引擎可消费的字符串参数
     * @author dz
     * @date 2026-06-26
     */
    private Map<String, String> normalizeParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        parameters.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                return;
            }
            if (value == null) {
                normalized.put(key, "");
            } else if (value instanceof String stringValue) {
                normalized.put(key, stringValue);
            } else if (value instanceof Number || value instanceof Boolean) {
                normalized.put(key, String.valueOf(value));
            } else {
                normalized.put(key, Jsons.toJson(value));
            }
        });
        return normalized;
    }

    /**
     * 分页查询资讯主题产品关联。
     *
     * <p>该接口用于前端解释 NEWS_HEAT 快照的热度来源。前端可以按主题编码、
     * 产品代码或资讯业务 ID 查询命中的关键词、来源质量分、综合关联分和证据摘要。</p>
     *
     * @param request 资讯、主题、产品和分页排序筛选条件
     * @return 资讯主题产品关联分页结果
     * @author dz
     * @date 2026-06-21
     */
    @PostMapping("/article-relations/list")
    @Operation(
        summary = "分页查询资讯主题产品关联",
        description = "查询新闻与投资主题、产品代码的显式关联。前端可用该接口展示资讯热度来源、命中关键词、来源质量分和关联证据。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回资讯主题产品关联分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<NewsArticleRelationResponse>> articleRelations(
        @Valid @RequestBody NewsArticleRelationListRequest request
    ) {
        return Result.success(PageResponse.from(tasks.articleRelations(
            request.articleBizId(),
            request.themeCode(),
            request.productCode(),
            request.relationType(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), NewsArticleRelationResponse::from));
    }

    /**
     * 分页查询投资方向收益、动量和热度快照。
     *
     * @param request 快照筛选和分页请求
     * @return 投资方向快照分页结果
     * @author dz
     * @date 2026-06-16
     */
    @PostMapping("/snapshots/list")
    @Operation(summary = "分页查询投资方向快照", description = "查询热门投资方向的实时收益、市场动量和资讯热度快照，供前端展示榜单和趋势卡片。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回投资方向快照分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<InvestmentThemeSnapshotResponse>> snapshots(
        @Valid @RequestBody InvestmentThemeSnapshotListRequest request
    ) {
        return Result.success(PageResponse.from(tasks.snapshots(
            request.taskCode(),
            request.snapshotType(),
            request.themeCode(),
            request.marketScope(),
            request.snapshotFrom(),
            request.snapshotTo(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "snapshotTime" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), InvestmentThemeSnapshotResponse::from));
    }
}
