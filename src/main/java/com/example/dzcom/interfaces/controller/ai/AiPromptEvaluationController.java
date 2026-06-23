package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.SaveAiPromptEvaluationCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiPromptEvaluationResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiPromptEvaluationBizIdRequest;
import com.example.dzcom.interfaces.request.ai.AiPromptEvaluationListRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiPromptEvaluationRequest;
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

/** AI Prompt 评估接口。 */
@RestController
@RequestMapping("/api/ai/prompt-evaluations")
@RequiredArgsConstructor
@Tag(name = "AI Prompt评估", description = "Prompt版本评分、回测反哺、反馈反哺和复核查询接口")
public class AiPromptEvaluationController {
    private final InvestmentClosedLoopApplicationService closedLoop;

    /** 保存 Prompt 评估。 */
    @PostMapping("/save")
    @Operation(summary = "保存AI Prompt评估", description = "保存人工或系统生成的 Prompt 版本评分，关联回测结果和反馈记录。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回 Prompt 评估", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或JSON格式不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptEvaluationResponse> save(@Valid @RequestBody SaveAiPromptEvaluationRequest request) {
        return Result.success(AiPromptEvaluationResponse.from(closedLoop.savePromptEvaluation(
            SaveAiPromptEvaluationCommand.builder()
                .promptBizId(request.promptBizId())
                .promptCode(request.promptCode())
                .promptVersion(request.promptVersion())
                .scenario(request.scenario())
                .backtestBizId(request.backtestBizId())
                .feedbackBizId(request.feedbackBizId())
                .score(request.score())
                .scoreDetail(request.scoreDetail())
                .reviewStatus(request.reviewStatus())
                .build())));
    }

    /** 查询 Prompt 评估详情。 */
    @PostMapping("/detail")
    @Operation(summary = "查询AI Prompt评估详情", description = "根据评估业务 ID 查询 Prompt 评分、回测、反馈和复核状态。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回 Prompt 评估详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Prompt评估不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptEvaluationResponse> detail(@Valid @RequestBody AiPromptEvaluationBizIdRequest request) {
        return Result.success(AiPromptEvaluationResponse.from(closedLoop.promptEvaluationDetail(request.bizId())));
    }

    /** 分页查询 Prompt 评估。 */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI Prompt评估", description = "按 Prompt 编码、版本、场景、回测、反馈和复核状态查询评估记录。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回 Prompt 评估分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<AiPromptEvaluationResponse>> list(
        @Valid @RequestBody AiPromptEvaluationListRequest request
    ) {
        return Result.success(PageResponse.from(closedLoop.listPromptEvaluations(
            request.promptCode(),
            request.promptVersion(),
            request.scenario(),
            request.backtestBizId(),
            request.feedbackBizId(),
            request.reviewStatus(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "evaluatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiPromptEvaluationResponse::from));
    }
}
