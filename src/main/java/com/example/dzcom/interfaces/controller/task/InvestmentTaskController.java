package com.example.dzcom.interfaces.controller.task;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.task.InvestmentTaskScheduleRefreshPort;
import com.example.dzcom.application.service.task.InvestmentTaskManagementService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskDefinitionResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskTriggerResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentThemeSnapshotResponse;
import com.example.dzcom.interfaces.dto.response.task.NewsArticleResponse;
import com.example.dzcom.interfaces.dto.response.task.ScheduledTaskExecutionResponse;
import com.example.dzcom.interfaces.request.task.InvestmentThemeSnapshotListRequest;
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

import java.util.List;

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
                request.parameters(),
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
            tasks.trigger(request.taskCode(), request.parameters(), "MANUAL")));
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
