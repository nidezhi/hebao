package com.example.dzcom.interfaces.controller.portfolio;

import com.example.dzcom.application.command.portfolio.CreateMockPortfolioCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockBuyCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.portfolio.MockPortfolioApplicationService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.portfolio.MockOrderExecutionResponse;
import com.example.dzcom.interfaces.dto.response.portfolio.MockPortfolioPerformanceResponse;
import com.example.dzcom.interfaces.dto.response.portfolio.MockPortfolioResponse;
import com.example.dzcom.interfaces.request.portfolio.CreateMockPortfolioRequest;
import com.example.dzcom.interfaces.request.portfolio.ExecuteMockBuyRequest;
import com.example.dzcom.interfaces.request.portfolio.ExecuteMockPlanFromReportRequest;
import com.example.dzcom.interfaces.request.portfolio.MockPortfolioDetailRequest;
import com.example.dzcom.interfaces.request.portfolio.MockPortfolioListRequest;
import com.example.dzcom.interfaces.request.portfolio.MockPortfolioPerformanceRequest;
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

/** 面向用户的模拟投资组合接口。 */
@RestController
@RequestMapping("/api/mock/portfolios")
@RequiredArgsConstructor
@Tag(name = "模拟投资组合", description = "创建、查询当前用户模拟组合、估值和持仓的接口")
public class MockPortfolioController {
    private final MockPortfolioApplicationService portfolios;

    /**
     * 创建当前用户的模拟组合。
     *
     * @param request 创建模拟组合请求
     * @return 创建后的模拟组合
     * @throws BusinessException 当用户未登录或参数不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/create")
    @Operation(
        summary = "创建模拟组合",
        description = "为当前登录用户创建 SIMULATION 类型组合，并写入初始现金估值快照，供前端展示模拟资产入口。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功，返回模拟组合响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或初始现金不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockPortfolioResponse> create(@Valid @RequestBody CreateMockPortfolioRequest request) {
        return Result.success(MockPortfolioResponse.from(portfolios.create(
            CreateMockPortfolioCommand.builder()
                .portfolioName(request.portfolioName())
                .baseCurrency(request.baseCurrency())
                .initialCash(request.initialCash())
                .build()
        )));
    }

    /**
     * 分页查询当前用户的模拟组合。
     *
     * @param request 分页和排序请求
     * @return 当前用户模拟组合分页结果
     * @throws BusinessException 当用户未登录或分页参数不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/mine")
    @Operation(
        summary = "分页查询我的模拟组合",
        description = "查询当前登录用户名下 SIMULATION 类型组合，列表项返回最新估值快照，positions 在列表页为空。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回模拟组合分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<MockPortfolioResponse>> mine(
        @Valid @RequestBody MockPortfolioListRequest request
    ) {
        return Result.success(PageResponse.from(portfolios.listMine(new PageQuery(
            request.page() == null ? 1 : request.page(),
            request.size() == null ? 20 : request.size(),
            request.sort() == null ? "createdAt" : request.sort(),
            request.direction() == null ? "desc" : request.direction()
        )), MockPortfolioResponse::from));
    }

    /**
     * 查询当前用户的模拟组合详情。
     *
     * @param request 模拟组合详情请求
     * @return 模拟组合详情
     * @throws BusinessException 当组合不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/detail")
    @Operation(
        summary = "查询模拟组合详情",
        description = "查询当前用户指定模拟组合的基础信息、最新估值和当前持仓集合，用于前端组合详情页。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回模拟组合详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "组合类型不支持或参数不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无权查看该模拟组合"),
        @ApiResponse(responseCode = "404", description = "模拟组合不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockPortfolioResponse> detail(@Valid @RequestBody MockPortfolioDetailRequest request) {
        return Result.success(MockPortfolioResponse.from(portfolios.detail(request.portfolioBizId())));
    }

    /**
     * 执行模拟金额买入。
     *
     * @param request 模拟买入请求
     * @return 模拟订单、成交和成交后的组合详情
     * @throws BusinessException 当组合、产品、行情、现金或 Mock 门禁不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/orders/buy")
    @Operation(
        summary = "执行模拟买入",
        description = "按产品最新1D收盘价模拟金额买入，写入订单、成交、持仓和新估值快照，不触发真实交易。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "买入成功，返回模拟订单执行结果", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法、产品不可Mock交易、行情缺失或现金不足"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无权操作该模拟组合"),
        @ApiResponse(responseCode = "404", description = "模拟组合或产品不存在"),
        @ApiResponse(responseCode = "409", description = "幂等订单状态异常"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockOrderExecutionResponse> buy(@Valid @RequestBody ExecuteMockBuyRequest request) {
        return Result.success(MockOrderExecutionResponse.from(portfolios.buy(
            ExecuteMockBuyCommand.builder()
                .portfolioBizId(request.portfolioBizId())
                .productBizId(request.productBizId())
                .amount(request.amount())
                .idempotencyKey(request.idempotencyKey())
                .build()
        )));
    }

    /**
     * 根据投资分析报告执行模拟买入。
     *
     * @param request 从报告执行模拟买入请求
     * @return 模拟订单、成交和成交后的组合详情
     * @throws BusinessException 当报告质量、产品、行情或现金不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/orders/buy-from-report")
    @Operation(
        summary = "根据投资分析报告执行模拟买入",
        description = "读取报告 investmentPlan.referenceAllocationAmount，并按报告主题或指定产品生成模拟买入订单。低质量报告和数据缺口报告会被拒绝。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "执行成功，返回模拟订单执行结果", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "报告质量不足、方案不可执行、产品不可Mock交易、行情缺失或现金不足"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无权操作该模拟组合"),
        @ApiResponse(responseCode = "404", description = "模拟组合、产品或报告不存在"),
        @ApiResponse(responseCode = "409", description = "幂等订单状态异常"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockOrderExecutionResponse> buyFromReport(
        @Valid @RequestBody ExecuteMockPlanFromReportRequest request
    ) {
        return Result.success(MockOrderExecutionResponse.from(portfolios.buyFromReport(
            ExecuteMockPlanFromReportCommand.builder()
                .portfolioBizId(request.portfolioBizId())
                .reportBizId(request.reportBizId())
                .productBizId(request.productBizId())
                .idempotencyKey(request.idempotencyKey())
                .build()
        )));
    }

    /**
     * 按当前持仓和最新行情刷新模拟组合估值。
     *
     * @param request 模拟组合详情请求
     * @return 刷新估值后的模拟组合详情
     * @throws BusinessException 当组合不存在、越权或持仓行情缺失时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/valuations/refresh")
    @Operation(
        summary = "刷新模拟组合估值",
        description = "按当前持仓和产品最新1D行情生成新的估值快照，返回刷新后的组合详情，用于前端手动刷新收益。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "刷新成功，返回模拟组合详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法或持仓产品缺少最新行情"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无权操作该模拟组合"),
        @ApiResponse(responseCode = "404", description = "模拟组合不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockPortfolioResponse> refreshValuation(
        @Valid @RequestBody MockPortfolioDetailRequest request
    ) {
        return Result.success(MockPortfolioResponse.from(portfolios.refreshValuation(request.portfolioBizId())));
    }

    /**
     * 查询模拟组合收益曲线。
     *
     * @param request 模拟组合收益曲线请求
     * @return 收益曲线和最大回撤
     * @throws BusinessException 当组合不存在、越权或分页参数不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/performance/curve")
    @Operation(
        summary = "查询模拟组合收益曲线",
        description = "查询组合估值历史，返回最新累计收益率、最大回撤和估值曲线点，用于前端绘制收益曲线。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回收益曲线响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无权查看该模拟组合"),
        @ApiResponse(responseCode = "404", description = "模拟组合不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MockPortfolioPerformanceResponse> performance(
        @Valid @RequestBody MockPortfolioPerformanceRequest request
    ) {
        return Result.success(MockPortfolioPerformanceResponse.from(
            portfolios.performance(request.portfolioBizId(), request.limit())
        ));
    }
}
