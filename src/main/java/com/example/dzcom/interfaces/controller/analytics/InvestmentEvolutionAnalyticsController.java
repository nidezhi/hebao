package com.example.dzcom.interfaces.controller.analytics;

import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.analytics.InvestmentEvolutionAnalyticsApplicationService;
import com.example.dzcom.interfaces.dto.response.analytics.InvestmentEvolutionSummaryResponse;
import com.example.dzcom.interfaces.request.analytics.InvestmentEvolutionSummaryRequest;
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

/** 投资闭环持续进化分析接口。 */
@RestController
@RequestMapping("/api/analytics/investment-evolution")
@RequiredArgsConstructor
@Tag(name = "投资闭环进化分析", description = "汇总闭环、Mock、风控、反馈、回测和模型调用审计，展示持续进化与可信任证据")
public class InvestmentEvolutionAnalyticsController {
    private final InvestmentEvolutionAnalyticsApplicationService analytics;

    /**
     * 查询投资闭环持续进化分析摘要。
     *
     * @param request 样本窗口请求
     * @return 进化分析摘要
     */
    @PostMapping("/summary")
    @Operation(summary = "查询投资闭环持续进化分析", description = "返回指标归集、模型调用稳定性、A/B归因、风控原因和样本限制。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回持续进化分析摘要", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "样本窗口参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentEvolutionSummaryResponse> summary(
        @Valid @RequestBody InvestmentEvolutionSummaryRequest request
    ) {
        return Result.success(InvestmentEvolutionSummaryResponse.from(analytics.summary(request.sampleSize())));
    }
}
