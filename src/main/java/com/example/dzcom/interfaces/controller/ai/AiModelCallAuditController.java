package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiModelCallAuditApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiModelCallAuditResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiModelCallAuditDetailRequest;
import com.example.dzcom.interfaces.request.ai.AiModelCallAuditListRequest;
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

/** AI 模型调用审计查询接口。 */
@RestController
@RequestMapping("/api/ai/model-call-audits")
@RequiredArgsConstructor
@Tag(name = "AI模型调用审计", description = "查询大模型调用埋点、业务关联、输入输出摘要和异常信息")
public class AiModelCallAuditController {
    private final AiModelCallAuditApplicationService audits;

    /** 分页查询 AI 模型调用审计。 */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI模型调用审计")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回模型调用审计分页数据", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法")
    })
    public Result<PageResponse<AiModelCallAuditResponse>> list(
        @Valid @RequestBody AiModelCallAuditListRequest request
    ) {
        return Result.success(PageResponse.from(audits.list(
            request.operationCode(),
            request.callStatus(),
            request.providerCode(),
            request.modelCode(),
            request.modelVersion(),
            request.businessType(),
            request.businessBizId(),
            request.taskCode(),
            request.eventId(),
            request.runBizId(),
            request.runNo(),
            request.reportBizId(),
            request.promptCode(),
            request.skillCode(),
            request.scenarioCode(),
            request.environment(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiModelCallAuditResponse::from));
    }

    /** 查询 AI 模型调用审计详情。 */
    @PostMapping("/detail")
    @Operation(summary = "查询AI模型调用审计详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回模型调用审计详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "模型调用审计不存在")
    })
    public Result<AiModelCallAuditResponse> detail(@Valid @RequestBody AiModelCallAuditDetailRequest request) {
        return Result.success(AiModelCallAuditResponse.from(audits.detail(request.bizId())));
    }
}
