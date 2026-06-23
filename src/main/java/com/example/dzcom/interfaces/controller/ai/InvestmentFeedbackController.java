package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.InvestmentFeedbackResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.InvestmentFeedbackBizIdRequest;
import com.example.dzcom.interfaces.request.ai.InvestmentFeedbackListRequest;
import com.example.dzcom.interfaces.request.ai.SaveInvestmentFeedbackRequest;
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

/** 投资反馈闭环接口。 */
@RestController
@RequestMapping("/api/ai/feedback")
@RequiredArgsConstructor
@Tag(name = "投资反馈", description = "报告、Prompt、Mock结果、回测结果和用户采纳拒绝反馈接口")
public class InvestmentFeedbackController {
    private final InvestmentClosedLoopApplicationService closedLoop;

    /** 保存投资反馈。 */
    @PostMapping("/save")
    @Operation(summary = "保存投资反馈", description = "保存用户采纳、拒绝、观察或忽略反馈，并在携带 Prompt 信息时自动生成 Prompt 评估。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回反馈记录", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或JSON格式不合法"),
        @ApiResponse(responseCode = "403", description = "关联回测结果不属于当前用户"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentFeedbackResponse> save(@Valid @RequestBody SaveInvestmentFeedbackRequest request) {
        return Result.success(InvestmentFeedbackResponse.from(closedLoop.saveFeedback(
            SaveInvestmentFeedbackCommand.builder()
                .targetType(request.targetType())
                .targetBizId(request.targetBizId())
                .reportBizId(request.reportBizId())
                .promptBizId(request.promptBizId())
                .promptCode(request.promptCode())
                .promptVersion(request.promptVersion())
                .backtestBizId(request.backtestBizId())
                .feedbackAction(request.feedbackAction())
                .reasonCode(request.reasonCode())
                .commentText(request.commentText())
                .metadata(request.metadata())
                .build())));
    }

    /** 查询反馈详情。 */
    @PostMapping("/detail")
    @Operation(summary = "查询投资反馈详情", description = "根据反馈业务 ID 查询用户动作、关联报告、Prompt、回测和备注。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回反馈详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "403", description = "反馈记录不属于当前用户"),
        @ApiResponse(responseCode = "404", description = "反馈不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<InvestmentFeedbackResponse> detail(@Valid @RequestBody InvestmentFeedbackBizIdRequest request) {
        return Result.success(InvestmentFeedbackResponse.from(closedLoop.feedbackDetail(request.bizId())));
    }

    /** 分页查询投资反馈。 */
    @PostMapping("/list")
    @Operation(summary = "分页查询投资反馈", description = "按目标、报告、Prompt、回测和动作查询当前用户反馈历史。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回反馈分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<InvestmentFeedbackResponse>> list(
        @Valid @RequestBody InvestmentFeedbackListRequest request
    ) {
        return Result.success(PageResponse.from(closedLoop.listFeedback(
            request.targetType(),
            request.targetBizId(),
            request.reportBizId(),
            request.promptCode(),
            request.promptVersion(),
            request.backtestBizId(),
            request.feedbackAction(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), InvestmentFeedbackResponse::from));
    }
}
