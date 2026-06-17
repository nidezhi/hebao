package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.InvestmentAnalysisApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.InvestmentAnalysisReportResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.GenerateInvestmentAnalysisRequest;
import com.example.dzcom.interfaces.request.ai.InvestmentAnalysisReportListRequest;
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

/**
 * 可插拔投资分析接口。
 *
 * <p>接口输出投资信息汇总、趋势、投资方案、模拟收益和前端图表数据，
 * 默认仅面向中国大陆投资主题。</p>
 */
@RestController
@RequestMapping("/api/investment/analysis")
@RequiredArgsConstructor
@Tag(name = "投资分析", description = "可插拔大模型投资分析、报告生成和图表数据查询接口")
public class InvestmentAnalysisController {
    private final InvestmentAnalysisApplicationService analysis;

    /**
     * 生成投资分析报告。
     *
     * @param request 分析请求
     * @return 投资分析报告
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/generate")
    @Operation(summary = "生成投资分析报告", description = "调用可插拔分析提供方生成投资信息汇总、趋势、投资方案、模拟收益和前端图表数据。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回投资分析报告", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或分析提供方不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentAnalysisReportResponse> generate(
        @Valid @RequestBody GenerateInvestmentAnalysisRequest request
    ) {
        return Result.success(InvestmentAnalysisReportResponse.from(analysis.generate(
            GenerateInvestmentAnalysisCommand.builder()
                .providerCode(request.providerCode())
                .modelCode(request.modelCode())
                .marketScope(request.marketScope())
                .themeCode(request.themeCode())
                .lookbackDays(request.lookbackDays())
                .initialCapital(request.initialCapital())
                .build()
        )));
    }

    /**
     * 分页查询投资分析报告。
     *
     * @param request 报告筛选和分页请求
     * @return 投资分析报告分页结果
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/reports/list")
    @Operation(summary = "分页查询投资分析报告", description = "按市场范围、主题、提供方和状态查询已生成报告，响应包含前端图表 JSON。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回投资分析报告分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<InvestmentAnalysisReportResponse>> reports(
        @Valid @RequestBody InvestmentAnalysisReportListRequest request
    ) {
        return Result.success(PageResponse.from(analysis.reports(
            request.marketScope(),
            request.themeCode(),
            request.providerCode(),
            request.status(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "generatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), InvestmentAnalysisReportResponse::from));
    }
}
