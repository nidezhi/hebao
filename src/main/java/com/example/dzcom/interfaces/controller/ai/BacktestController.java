package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveBacktestResultCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.BacktestResultResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.BacktestBizIdRequest;
import com.example.dzcom.interfaces.request.ai.BacktestListRequest;
import com.example.dzcom.interfaces.request.ai.GenerateBacktestFromPortfolioRequest;
import com.example.dzcom.interfaces.request.ai.SaveBacktestResultRequest;
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

/** 回测结果接口。 */
@RestController
@RequestMapping("/api/backtests")
@RequiredArgsConstructor
@Tag(name = "回测结果", description = "策略回测结果保存、Mock组合回测摘要生成、详情和列表接口")
public class BacktestController {
    private final InvestmentClosedLoopApplicationService closedLoop;

    /** 保存回测结果。 */
    @PostMapping("/save")
    @Operation(summary = "保存回测结果", description = "保存外部回测或后台计算得到的回测任务、参数和指标摘要。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回回测结果", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或JSON格式不合法"),
        @ApiResponse(responseCode = "403", description = "更新的回测结果不属于当前用户"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<BacktestResultResponse> save(@Valid @RequestBody SaveBacktestResultRequest request) {
        return Result.success(BacktestResultResponse.from(closedLoop.saveBacktest(SaveBacktestResultCommand.builder()
            .bizId(request.bizId())
            .strategyCode(request.strategyCode())
            .strategyVersion(request.strategyVersion())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .initialCapital(request.initialCapital())
            .benchmarkCode(request.benchmarkCode())
            .parameters(request.parameters())
            .metrics(request.metrics())
            .resultUri(request.resultUri())
            .status(request.status())
            .failureReason(request.failureReason())
            .startedAt(request.startedAt())
            .completedAt(request.completedAt())
            .build())));
    }

    /** 从模拟组合生成回测摘要。 */
    @PostMapping("/generate-from-portfolio")
    @Operation(summary = "从模拟组合生成回测摘要", description = "读取 Mock 组合估值曲线，生成收益率、最大回撤和波动率等回测摘要。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "生成成功，返回回测结果", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "估值点不足或参数不合法"),
        @ApiResponse(responseCode = "403", description = "模拟组合不属于当前用户"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<BacktestResultResponse> generateFromPortfolio(
        @Valid @RequestBody GenerateBacktestFromPortfolioRequest request
    ) {
        return Result.success(BacktestResultResponse.from(closedLoop.generateBacktestFromPortfolio(
            GenerateBacktestFromPortfolioCommand.builder()
                .portfolioBizId(request.portfolioBizId())
                .strategyCode(request.strategyCode())
                .strategyVersion(request.strategyVersion())
                .benchmarkCode(request.benchmarkCode())
                .parameters(request.parameters())
                .limit(request.limit())
                .build())));
    }

    /** 查询回测详情。 */
    @PostMapping("/detail")
    @Operation(summary = "查询回测详情", description = "根据回测业务 ID 查询回测参数、指标和状态。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回回测详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "403", description = "回测结果不属于当前用户"),
        @ApiResponse(responseCode = "404", description = "回测结果不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<BacktestResultResponse> detail(@Valid @RequestBody BacktestBizIdRequest request) {
        return Result.success(BacktestResultResponse.from(closedLoop.backtestDetail(request.bizId())));
    }

    /** 分页查询回测结果。 */
    @PostMapping("/list")
    @Operation(summary = "分页查询回测结果", description = "按策略编码、版本和状态查询当前用户可见的回测结果。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回回测分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<BacktestResultResponse>> list(@Valid @RequestBody BacktestListRequest request) {
        return Result.success(PageResponse.from(closedLoop.listBacktests(
            request.strategyCode(),
            request.strategyVersion(),
            request.status(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), BacktestResultResponse::from));
    }
}
